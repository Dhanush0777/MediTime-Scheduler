package com.medtime.controller;

import com.medtime.dto.ApiResponse;
import com.medtime.dto.MedicineScheduleDto;
import com.medtime.dto.SnoozeRequest;
import com.medtime.entity.Patient;
import com.medtime.entity.User;
import com.medtime.repository.PatientRepository;
import com.medtime.service.AuthService;
import com.medtime.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleRestController {

    private final ScheduleService scheduleService;
    private final AuthService authService;
    private final PatientRepository patientRepository;

    public ScheduleRestController(ScheduleService scheduleService,
                                  AuthService authService,
                                  PatientRepository patientRepository) {
        this.scheduleService = scheduleService;
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
        // Fallback to first patient if no auth session
        return patientRepository.findAll().stream().findFirst().map(Patient::getId).orElse(1L);
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<MedicineScheduleDto>>> getTodaySchedule(
            @RequestParam(required = false) Long patientId) {
        Long resolvedId = resolveCurrentPatientId(patientId);
        List<MedicineScheduleDto> list = scheduleService.getTodaySchedule(resolvedId);
        return ResponseEntity.ok(ApiResponse.ok("Today's medicine timetable", list));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<MedicineScheduleDto>>> getScheduleByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long patientId) {
        Long resolvedId = resolveCurrentPatientId(patientId);
        List<MedicineScheduleDto> list = scheduleService.getScheduleByDate(resolvedId, date);
        return ResponseEntity.ok(ApiResponse.ok("Medicine timetable for " + date, list));
    }

    @GetMapping("/active-alarms")
    public ResponseEntity<ApiResponse<List<MedicineScheduleDto>>> getActiveAlarms(
            @RequestParam(required = false) Long patientId) {
        Long resolvedId = resolveCurrentPatientId(patientId);
        List<MedicineScheduleDto> alarms = scheduleService.getActiveAlarms(resolvedId);
        return ResponseEntity.ok(ApiResponse.ok("Active reminder alarms", alarms));
    }

    @GetMapping("/next-dose")
    public ResponseEntity<ApiResponse<MedicineScheduleDto>> getNextDose(
            @RequestParam(required = false) Long patientId) {
        Long resolvedId = resolveCurrentPatientId(patientId);
        Optional<MedicineScheduleDto> nextDose = scheduleService.getNextDose(resolvedId);
        return nextDose.map(dto -> ResponseEntity.ok(ApiResponse.ok("Next scheduled dose", dto)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.ok("No upcoming doses scheduled", null)));
    }

    @PutMapping("/{id}/taken")
    public ResponseEntity<ApiResponse<MedicineScheduleDto>> markTaken(@PathVariable Long id) {
        MedicineScheduleDto updated = scheduleService.markTaken(id);
        return ResponseEntity.ok(ApiResponse.ok("Medicine marked as TAKEN at " + updated.getTakenAt(), updated));
    }

    @PutMapping("/{id}/missed")
    public ResponseEntity<ApiResponse<MedicineScheduleDto>> markMissed(@PathVariable Long id) {
        MedicineScheduleDto updated = scheduleService.markMissed(id);
        return ResponseEntity.ok(ApiResponse.ok("Medicine marked as MISSED", updated));
    }

    @PutMapping("/{id}/snooze")
    public ResponseEntity<ApiResponse<MedicineScheduleDto>> snoozeSchedule(
            @PathVariable Long id,
            @RequestBody(required = false) SnoozeRequest request) {
        int minutes = request != null && request.getMinutes() > 0 ? request.getMinutes() : 10;
        MedicineScheduleDto updated = scheduleService.snoozeSchedule(id, minutes);
        return ResponseEntity.ok(ApiResponse.ok("Reminder snoozed for " + minutes + " minutes", updated));
    }
}
