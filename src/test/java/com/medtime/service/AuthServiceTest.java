package com.medtime.service;

import com.medtime.dto.AuthRequest;
import com.medtime.dto.AuthResponse;
import com.medtime.dto.RegisterRequest;
import com.medtime.entity.Doctor;
import com.medtime.entity.Patient;
import com.medtime.entity.Role;
import com.medtime.entity.User;
import com.medtime.exception.BadRequestException;
import com.medtime.exception.UnauthorizedException;
import com.medtime.repository.DoctorRepository;
import com.medtime.repository.PatientRepository;
import com.medtime.repository.ReminderSettingRepository;
import com.medtime.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private ReminderSettingRepository reminderSettingRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("Dr. Test", "doctor@test.com", "encodedPassword", Role.DOCTOR, "1234567890");
        sampleUser.setId(1L);
    }

    @Test
    void testRegisterDoctorSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setName("Dr. Test");
        req.setEmail("doctor@test.com");
        req.setPassword("plainPassword");
        req.setRole(Role.DOCTOR);
        req.setSpecialization("Cardiologist");

        when(userRepository.existsByEmail("doctor@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        Doctor doc = new Doctor(sampleUser, "Cardiologist", "MED-1", "Hospital", "MBBS");
        doc.setId(10L);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(doc);

        AuthResponse response = authService.register(req);

        assertNotNull(response);
        assertEquals("doctor@test.com", response.getEmail());
        assertEquals(Role.DOCTOR, response.getRole());
        assertEquals(10L, response.getProfileId());
    }

    @Test
    void testRegisterDuplicateEmailThrowsBadRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("doctor@test.com");
        when(userRepository.existsByEmail("doctor@test.com")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(req));
    }

    @Test
    void testLoginSuccess() {
        AuthRequest req = new AuthRequest("doctor@test.com", "plainPassword");
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("plainPassword", "encodedPassword")).thenReturn(true);

        Doctor doc = new Doctor();
        doc.setId(10L);
        when(doctorRepository.findByUser(sampleUser)).thenReturn(Optional.of(doc));

        AuthResponse response = authService.login(req);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals(10L, response.getProfileId());
    }

    @Test
    void testLoginInvalidPasswordThrowsUnauthorized() {
        AuthRequest req = new AuthRequest("doctor@test.com", "wrongPassword");
        when(userRepository.findByEmail("doctor@test.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(req));
    }
}
