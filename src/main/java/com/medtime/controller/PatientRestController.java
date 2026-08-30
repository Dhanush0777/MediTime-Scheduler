package com.medtime.controller;

import com.medtime.dto.AdherenceStatsDto;
import com.medtime.dto.ApiResponse;
import com.medtime.dto.MedicineScheduleDto;
import com.medtime.dto.PatientDto;
import com.medtime.service.AdherenceService;
import com.medtime.service.PatientService;
import com.medtime.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientRestController {

    private final PatientService patientService;
    private final ScheduleService scheduleService;
    private final AdherenceService adherenceService;

    public PatientRestController(PatientService patientService,
                                 ScheduleService scheduleService,
                                 AdherenceService adherenceService) {
        this.patientService = patientService;
        this.scheduleService = scheduleService;
        this.adherenceService = adherenceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientDto>>> getAllPatients() {
        List<PatientDto> patients = patientService.getAllPatients();
        return ResponseEntity.ok(ApiResponse.ok("Patients list", patients));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PatientDto>> getPatientById(@PathVariable Long id) {
        PatientDto patient = patientService.getPatientDtoById(id);
        return ResponseEntity.ok(ApiResponse.ok("Patient profile", patient));
    }

    @GetMapping("/{id}/today")
    public ResponseEntity<ApiResponse<List<MedicineScheduleDto>>> getTodaySchedule(@PathVariable Long id) {
        List<MedicineScheduleDto> list = scheduleService.getTodaySchedule(id);
        return ResponseEntity.ok(ApiResponse.ok("Today's medicine timetable", list));
    }

    @GetMapping("/{id}/timetable")
    public ResponseEntity<ApiResponse<List<MedicineScheduleDto>>> getTimetable(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<MedicineScheduleDto> list = scheduleService.getScheduleByDate(id, targetDate);
        return ResponseEntity.ok(ApiResponse.ok("Medicine timetable for " + targetDate, list));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<ApiResponse<List<MedicineScheduleDto>>> getMedicationHistory(@PathVariable Long id) {
        List<MedicineScheduleDto> list = scheduleService.getMedicationHistory(id);
        return ResponseEntity.ok(ApiResponse.ok("Medication history", list));
    }

    @GetMapping("/{id}/statistics")
    public ResponseEntity<ApiResponse<AdherenceStatsDto>> getStatistics(@PathVariable Long id) {
        AdherenceStatsDto stats = adherenceService.getPatientAdherenceStats(id);
        return ResponseEntity.ok(ApiResponse.ok("Adherence statistics", stats));
    }
}
