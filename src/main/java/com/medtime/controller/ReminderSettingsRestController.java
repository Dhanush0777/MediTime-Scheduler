package com.medtime.controller;

import com.medtime.dto.ApiResponse;
import com.medtime.dto.ReminderSettingsDto;
import com.medtime.entity.Patient;
import com.medtime.entity.User;
import com.medtime.repository.PatientRepository;
import com.medtime.service.AuthService;
import com.medtime.service.ReminderSettingsService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/settings/reminder")
public class ReminderSettingsRestController {

    private final ReminderSettingsService reminderSettingsService;
    private final AuthService authService;
    private final PatientRepository patientRepository;

    public ReminderSettingsRestController(ReminderSettingsService reminderSettingsService,
                                          AuthService authService,
                                          PatientRepository patientRepository) {
        this.reminderSettingsService = reminderSettingsService;
        this.authService = authService;
        this.patientRepository = patientRepository;
    }

    private Long resolveCurrentPatientId(Long explicitPatientId) {
        if (explicitPatientId != null) {
            return explicitPatientId;
        }
        Optional<User> currentUser = authService.getCurrentAuthenticatedUser();
        if (currentUser.isPresent()) {
            Optional<Patient> patientOpt = patientRepository.findByUser(currentUser.get());
            if (patientOpt.isPresent()) {
                return patientOpt.get().getId();
            }
        }
        return patientRepository.findAll().stream().findFirst().map(Patient::getId).orElse(1L);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ReminderSettingsDto>> getSettings(
            @RequestParam(required = false) Long patientId) {
        Long resolvedId = resolveCurrentPatientId(patientId);
        ReminderSettingsDto settings = reminderSettingsService.getSettings(resolvedId);
        return ResponseEntity.ok(ApiResponse.ok("Reminder settings", settings));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ReminderSettingsDto>> updateSettings(
            @RequestParam(required = false) Long patientId,
            @Valid @RequestBody ReminderSettingsDto request) {
        Long resolvedId = resolveCurrentPatientId(patientId);
        ReminderSettingsDto updated = reminderSettingsService.updateSettings(resolvedId, request);
        return ResponseEntity.ok(ApiResponse.ok("Reminder settings updated successfully", updated));
    }
}
