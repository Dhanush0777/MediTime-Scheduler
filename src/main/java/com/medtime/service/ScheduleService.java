package com.medtime.service;

import com.medtime.dto.MedicineScheduleDto;
import com.medtime.entity.Medicine;
import com.medtime.entity.MedicineSchedule;
import com.medtime.entity.Prescription;
import com.medtime.entity.ScheduleStatus;
import com.medtime.exception.BadRequestException;
import com.medtime.exception.ResourceNotFoundException;
import com.medtime.repository.MedicineScheduleRepository;
import com.medtime.util.ScheduleTimeHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ScheduleService {

    private final MedicineScheduleRepository scheduleRepository;

    public ScheduleService(MedicineScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public List<MedicineScheduleDto> getTodaySchedule(Long patientId) {
        return getScheduleByDate(patientId, LocalDate.now());
    }

    public List<MedicineScheduleDto> getScheduleByDate(Long patientId, LocalDate date) {
        List<MedicineSchedule> schedules = scheduleRepository.findByPatientIdAndScheduledDateOrderByScheduledTimeAsc(patientId, date);
        return schedules.stream()
                .map(this::convertToDto)
                .sorted(Comparator.comparing(MedicineScheduleDto::getScheduledTime))
                .collect(Collectors.toList());
    }

    public List<MedicineScheduleDto> getMedicationHistory(Long patientId) {
        List<MedicineSchedule> schedules = scheduleRepository.findByPatientIdOrderByScheduledDateDescScheduledTimeDesc(patientId);
        return schedules.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<MedicineScheduleDto> getActiveAlarms(Long patientId) {
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        String currentHHmm = nowTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime nowDateTime = LocalDateTime.now();

        List<MedicineSchedule> todaySchedules = scheduleRepository.findByPatientIdAndScheduledDateOrderByScheduledTimeAsc(patientId, today);

        return todaySchedules.stream()
                .filter(s -> {
                    if (s.getStatus() == ScheduleStatus.PENDING) {
                        // Due if scheduledTime <= currentHHmm
                        return s.getScheduledTime().compareTo(currentHHmm) <= 0;
                    } else if (s.getStatus() == ScheduleStatus.SNOOZED) {
                        return s.getSnoozedUntil() != null && !s.getSnoozedUntil().isAfter(nowDateTime);
                    }
                    return false;
                })
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Optional<MedicineScheduleDto> getNextDose(Long patientId) {
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        String currentHHmm = nowTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        // Check remaining today
        List<MedicineSchedule> todaySchedules = scheduleRepository.findByPatientIdAndScheduledDateOrderByScheduledTimeAsc(patientId, today);
        for (MedicineSchedule s : todaySchedules) {
            if ((s.getStatus() == ScheduleStatus.PENDING || s.getStatus() == ScheduleStatus.SNOOZED)) {
                // If it's pending in the future or active now
                return Optional.of(convertToDto(s));
            }
        }

        // Otherwise check future dates
        List<MedicineSchedule> futureSchedules = scheduleRepository.findByPatientIdAndScheduledDateBetweenOrderByScheduledDateAscScheduledTimeAsc(
                patientId, today.plusDays(1), today.plusDays(7));
        if (!futureSchedules.isEmpty()) {
            return Optional.of(convertToDto(futureSchedules.get(0)));
        }

        return Optional.empty();
    }

    @Transactional
    public MedicineScheduleDto markTaken(Long scheduleId) {
        MedicineSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule item not found with id: " + scheduleId));

        schedule.setStatus(ScheduleStatus.TAKEN);
        schedule.setTakenAt(LocalDateTime.now());
        schedule.setSnoozedUntil(null);
        MedicineSchedule saved = scheduleRepository.save(schedule);
        return convertToDto(saved);
    }

    @Transactional
    public MedicineScheduleDto markMissed(Long scheduleId) {
        MedicineSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule item not found with id: " + scheduleId));

        schedule.setStatus(ScheduleStatus.MISSED);
        schedule.setSnoozedUntil(null);
        MedicineSchedule saved = scheduleRepository.save(schedule);
        return convertToDto(saved);
    }

    @Transactional
    public MedicineScheduleDto snoozeSchedule(Long scheduleId, int minutes) {
        if (minutes <= 0) {
            minutes = 10;
        }
        MedicineSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule item not found with id: " + scheduleId));

        schedule.setStatus(ScheduleStatus.SNOOZED);
        schedule.setSnoozedUntil(LocalDateTime.now().plusMinutes(minutes));
        MedicineSchedule saved = scheduleRepository.save(schedule);
        return convertToDto(saved);
    }

    public MedicineScheduleDto convertToDto(MedicineSchedule s) {
        MedicineScheduleDto dto = new MedicineScheduleDto();
        dto.setId(s.getId());
        dto.setScheduledDate(s.getScheduledDate());
        dto.setScheduledTime(s.getScheduledTime());
        dto.setScheduledTimeFormatted(ScheduleTimeHelper.formatTo12Hour(s.getScheduledTime()));
        dto.setStatus(s.getStatus());
        dto.setTakenAt(s.getTakenAt());
        dto.setSnoozedUntil(s.getSnoozedUntil());
        dto.setNotes(s.getNotes());

        if (s.getPatient() != null) {
            dto.setPatientId(s.getPatient().getId());
            if (s.getPatient().getUser() != null) {
                dto.setPatientName(s.getPatient().getUser().getName());
            }
        }

        Medicine med = s.getMedicine();
        if (med != null) {
            dto.setMedicineId(med.getId());
            dto.setMedicineName(med.getMedicineName());
            dto.setMedicineType(med.getMedicineType());
            dto.setDosage(med.getDosage());
            dto.setMealInstruction(med.getMealInstruction());
            dto.setSpecialInstruction(med.getSpecialInstruction());

            Prescription p = med.getPrescription();
            if (p != null) {
                dto.setPrescriptionId(p.getId());
                if (p.getDoctor() != null && p.getDoctor().getUser() != null) {
                    dto.setDoctorName(p.getDoctor().getUser().getName());
                }
            }
        }

        // Check if due right now
        LocalDate today = LocalDate.now();
        LocalTime nowTime = LocalTime.now();
        String currentHHmm = nowTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        LocalDateTime nowDateTime = LocalDateTime.now();

        boolean isDue = false;
        if (s.getScheduledDate().isEqual(today)) {
            if (s.getStatus() == ScheduleStatus.PENDING && s.getScheduledTime().compareTo(currentHHmm) <= 0) {
                isDue = true;
            } else if (s.getStatus() == ScheduleStatus.SNOOZED && s.getSnoozedUntil() != null && !s.getSnoozedUntil().isAfter(nowDateTime)) {
                isDue = true;
            }
        }
        dto.setDue(isDue);

        return dto;
    }
}
