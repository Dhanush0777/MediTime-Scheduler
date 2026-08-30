package com.medtime.service;

import com.medtime.dto.DoctorDto;
import com.medtime.entity.Doctor;
import com.medtime.exception.ResourceNotFoundException;
import com.medtime.repository.DoctorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public Doctor getDoctorEntity(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
    }

    public Doctor getDoctorByUserId(Long userId) {
        return doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user id: " + userId));
    }

    public DoctorDto getDoctorDtoById(Long id) {
        Doctor doctor = getDoctorEntity(id);
        return convertToDto(doctor);
    }

    public List<DoctorDto> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public DoctorDto convertToDto(Doctor d) {
        DoctorDto dto = new DoctorDto();
        dto.setId(d.getId());
        if (d.getUser() != null) {
            dto.setUserId(d.getUser().getId());
            dto.setName(d.getUser().getName());
            dto.setEmail(d.getUser().getEmail());
            dto.setPhone(d.getUser().getPhone());
        }
        dto.setSpecialization(d.getSpecialization());
        dto.setLicenseNumber(d.getLicenseNumber());
        dto.setHospitalAffiliation(d.getHospitalAffiliation());
        dto.setQualification(d.getQualification());
        return dto;
    }
}
