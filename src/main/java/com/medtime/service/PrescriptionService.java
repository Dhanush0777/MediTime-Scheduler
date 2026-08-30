package com.medtime.service;

import com.medtime.dto.MedicineDto;
import com.medtime.dto.PrescriptionRequest;
import com.medtime.dto.PrescriptionResponseDto;
import com.medtime.entity.*;
import com.medtime.exception.BadRequestException;
import com.medtime.exception.ResourceNotFoundException;
import com.medtime.repository.DoctorRepository;
import com.medtime.repository.MedicineRepository;
import com.medtime.repository.MedicineScheduleRepository;
import com.medtime.repository.PatientRepository;
import com.medtime.repository.PrescriptionRepository;
import com.medtime.util.ScheduleTimeHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineScheduleRepository scheduleRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               MedicineRepository medicineRepository,
                               MedicineScheduleRepository scheduleRepository,
                               DoctorRepository doctorRepository,
                               PatientRepository patientRepository) {
        this.prescriptionRepository = prescriptionRepository;
        this.medicineRepository = medicineRepository;
        this.scheduleRepository = scheduleRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public PrescriptionResponseDto createPrescription(PrescriptionRequest request, Long authenticatedDoctorId) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            throw new BadRequestException("Start date and end date are required");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date");
        }
        if (request.getMedicines() == null || request.getMedicines().isEmpty()) {
            throw new BadRequestException("Prescription must contain at least one medicine");
        }

        Doctor doctor;
        if (authenticatedDoctorId != null) {
            doctor = doctorRepository.findById(authenticatedDoctorId)
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + authenticatedDoctorId));
        } else if (request.getDoctorId() != null) {
            doctor = doctorRepository.findById(request.getDoctorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));
        } else {
            // Default to first available doctor if not provided
            doctor = doctorRepository.findAll().stream().findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("No registered doctor found"));
        }

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        Prescription prescription = new Prescription();
        prescription.setDoctor(doctor);
        prescription.setPatient(patient);
        prescription.setPrescriptionDate(request.getPrescriptionDate() != null ? request.getPrescriptionDate() : LocalDate.now());
        prescription.setStartDate(request.getStartDate());
        prescription.setEndDate(request.getEndDate());
        prescription.setDiagnosis(request.getDiagnosis().trim());
        prescription.setNotes(request.getNotes());

        Prescription savedPrescription = prescriptionRepository.save(prescription);

        List<MedicineSchedule> schedulesToSave = new ArrayList<>();

        for (MedicineDto medDto : request.getMedicines()) {
            Medicine medicine = new Medicine();
            medicine.setPrescription(savedPrescription);
            medicine.setMedicineName(medDto.getMedicineName().trim());
            medicine.setMedicineType(medDto.getMedicineType() != null ? medDto.getMedicineType() : "Tablet");
            medicine.setDosage(medDto.getDosage().trim());
            medicine.setFrequency(medDto.getFrequency().trim());
            medicine.setDurationDays(medDto.getDurationDays() > 0 ? medDto.getDurationDays() : 5);
            medicine.setMealInstruction(medDto.getMealInstruction() != null ? medDto.getMealInstruction() : "After food");
            medicine.setSpecialInstruction(medDto.getSpecialInstruction());

            List<String> times = medDto.getReminderTimes();
            if (times == null || times.isEmpty()) {
                times = ScheduleTimeHelper.getDefaultTimesForFrequency(medDto.getFrequency());
            }
            times = ScheduleTimeHelper.normalizeAndSortTimes(times);
            medicine.setReminderTimes(times);

            Medicine savedMedicine = medicineRepository.save(medicine);

            // Generate daily schedules
            LocalDate curDate = request.getStartDate();
            int daysCount = 0;
            while (!curDate.isAfter(request.getEndDate()) && daysCount < medicine.getDurationDays()) {
                for (String time : times) {
                    MedicineSchedule schedule = new MedicineSchedule(savedMedicine, patient, curDate, time);
                    schedulesToSave.add(schedule);
                }
                curDate = curDate.plusDays(1);
                daysCount++;
            }
        }

        if (!schedulesToSave.isEmpty()) {
            scheduleRepository.saveAll(schedulesToSave);
        }

        return getPrescriptionDtoById(savedPrescription.getId());
    }

    public PrescriptionResponseDto getPrescriptionDtoById(Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found with id: " + id));
        return convertToDto(prescription);
    }

    public List<PrescriptionResponseDto> getPrescriptionsByDoctor(Long doctorId) {
        return prescriptionRepository.findByDoctorIdOrderByPrescriptionDateDesc(doctorId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PrescriptionResponseDto> getPrescriptionsByPatient(Long patientId) {
        return prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(patientId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<PrescriptionResponseDto> getAllPrescriptions() {
        return prescriptionRepository.findAllByOrderByPrescriptionDateDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePrescription(Long id) {
        if (!prescriptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Prescription not found with id: " + id);
        }
        scheduleRepository.deleteByPrescriptionId(id);
        prescriptionRepository.deleteById(id);
    }

    public PrescriptionResponseDto convertToDto(Prescription p) {
        PrescriptionResponseDto dto = new PrescriptionResponseDto();
        dto.setId(p.getId());
        if (p.getDoctor() != null) {
            dto.setDoctorId(p.getDoctor().getId());
            dto.setDoctorName(p.getDoctor().getUser() != null ? p.getDoctor().getUser().getName() : "Dr. Medical Specialist");
            dto.setDoctorSpecialization(p.getDoctor().getSpecialization());
        }
        if (p.getPatient() != null) {
            dto.setPatientId(p.getPatient().getId());
            dto.setPatientName(p.getPatient().getUser() != null ? p.getPatient().getUser().getName() : "Patient");
        }
        dto.setPrescriptionDate(p.getPrescriptionDate());
        dto.setStartDate(p.getStartDate());
        dto.setEndDate(p.getEndDate());
        dto.setDiagnosis(p.getDiagnosis());
        dto.setNotes(p.getNotes());
        dto.setCreatedAt(p.getCreatedAt());

        List<Medicine> medicines = medicineRepository.findByPrescriptionId(p.getId());
        List<MedicineDto> medDtos = medicines.stream().map(m -> {
            MedicineDto mDto = new MedicineDto();
            mDto.setId(m.getId());
            mDto.setMedicineName(m.getMedicineName());
            mDto.setMedicineType(m.getMedicineType());
            mDto.setDosage(m.getDosage());
            mDto.setFrequency(m.getFrequency());
            mDto.setDurationDays(m.getDurationDays());
            mDto.setMealInstruction(m.getMealInstruction());
            mDto.setSpecialInstruction(m.getSpecialInstruction());
            mDto.setReminderTimes(m.getReminderTimes());
            return mDto;
        }).collect(Collectors.toList());

        dto.setMedicines(medDtos);
        return dto;
    }
}
