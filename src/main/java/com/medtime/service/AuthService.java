package com.medtime.service;

import com.medtime.dto.AuthRequest;
import com.medtime.dto.AuthResponse;
import com.medtime.dto.RegisterRequest;
import com.medtime.entity.*;
import com.medtime.exception.BadRequestException;
import com.medtime.exception.ResourceNotFoundException;
import com.medtime.exception.UnauthorizedException;
import com.medtime.repository.DoctorRepository;
import com.medtime.repository.PatientRepository;
import com.medtime.repository.ReminderSettingRepository;
import com.medtime.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final ReminderSettingRepository reminderSettingRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       DoctorRepository doctorRepository,
                       PatientRepository patientRepository,
                       ReminderSettingRepository reminderSettingRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.reminderSettingRepository = reminderSettingRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        user = userRepository.save(user);

        Long profileId = null;

        if (request.getRole() == Role.DOCTOR) {
            Doctor doctor = new Doctor();
            doctor.setUser(user);
            doctor.setSpecialization(request.getSpecialization() != null ? request.getSpecialization() : "General Physician");
            doctor.setLicenseNumber(request.getLicenseNumber() != null ? request.getLicenseNumber() : "MED-" + System.currentTimeMillis() % 100000);
            doctor.setHospitalAffiliation(request.getHospitalAffiliation() != null ? request.getHospitalAffiliation() : "City Medical Center");
            doctor.setQualification(request.getQualification() != null ? request.getQualification() : "MBBS, MD");
            doctor = doctorRepository.save(doctor);
            profileId = doctor.getId();
        } else if (request.getRole() == Role.PATIENT) {
            Patient patient = new Patient();
            patient.setUser(user);
            patient.setDateOfBirth(request.getDateOfBirth());
            patient.setGender(request.getGender() != null ? request.getGender() : "Not specified");
            patient.setBloodGroup(request.getBloodGroup() != null ? request.getBloodGroup() : "O+");
            patient.setEmergencyContact(request.getEmergencyContact());
            patient.setAddress(request.getAddress());
            patient = patientRepository.save(patient);

            ReminderSetting setting = new ReminderSetting(patient);
            reminderSettingRepository.save(setting);
            profileId = patient.getId();
        }

        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), profileId, null);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        Long profileId = null;
        if (user.getRole() == Role.DOCTOR) {
            profileId = doctorRepository.findByUser(user).map(Doctor::getId).orElse(null);
        } else if (user.getRole() == Role.PATIENT) {
            profileId = patientRepository.findByUser(user).map(Patient::getId).orElse(null);
        }

        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), profileId, null);
    }

    public Optional<User> getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        return userRepository.findByEmail(auth.getName().toLowerCase());
    }

    public User getRequiredCurrentUser() {
        return getCurrentAuthenticatedUser()
                .orElseThrow(() -> new UnauthorizedException("User is not authenticated"));
    }
}
