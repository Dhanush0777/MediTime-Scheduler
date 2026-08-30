package com.medtime.service;

import com.medtime.dto.PatientDto;
import com.medtime.entity.Patient;
import com.medtime.entity.ScheduleStatus;
import com.medtime.exception.ResourceNotFoundException;
import com.medtime.repository.MedicineScheduleRepository;
import com.medtime.repository.PatientRepository;
import com.medtime.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final MedicineScheduleRepository scheduleRepository;

    public PatientService(PatientRepository patientRepository,
                          PrescriptionRepository prescriptionRepository,
                          MedicineScheduleRepository scheduleRepository) {
        this.patientRepository = patientRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public List<PatientDto> getAllPatients() {
        return patientRepository.findAllByOrderByIdDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Patient getPatientEntity(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    public Patient getPatientByUserId(Long userId) {
        return patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user id: " + userId));
    }

    public PatientDto getPatientDtoById(Long id) {
        Patient patient = getPatientEntity(id);
        return convertToDto(patient);
    }

    public PatientDto convertToDto(Patient p) {
        PatientDto dto = new PatientDto();
        dto.setId(p.getId());
        if (p.getUser() != null) {
            dto.setUserId(p.getUser().getId());
            dto.setName(p.getUser().getName());
            dto.setEmail(p.getUser().getEmail());
            dto.setPhone(p.getUser().getPhone());
        }
        dto.setDateOfBirth(p.getDateOfBirth());
        dto.setGender(p.getGender());
        dto.setBloodGroup(p.getBloodGroup());
        dto.setEmergencyContact(p.getEmergencyContact());
        dto.setAddress(p.getAddress());

        if (p.getDateOfBirth() != null) {
            dto.setAge(Period.between(p.getDateOfBirth(), LocalDate.now()).getYears());
        }

        long prescriptionCount = prescriptionRepository.findByPatientIdOrderByPrescriptionDateDesc(p.getId()).size();
        dto.setTotalPrescriptions(prescriptionCount);

        long totalSchedules = scheduleRepository.countByPatientId(p.getId());
        long takenSchedules = scheduleRepository.countByPatientIdAndStatus(p.getId(), ScheduleStatus.TAKEN);
        double adherence = totalSchedules > 0 ? ((double) takenSchedules / totalSchedules) * 100.0 : 100.0;
        dto.setOverallAdherence(Math.round(adherence * 10.0) / 10.0);

        return dto;
    }
}
