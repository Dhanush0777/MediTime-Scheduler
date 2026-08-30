package com.medtime.controller;

import com.medtime.dto.*;
import com.medtime.entity.Patient;
import com.medtime.entity.User;
import com.medtime.repository.PatientRepository;
import com.medtime.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/patient")
public class PatientViewController {

    private final AuthService authService;
    private final PatientRepository patientRepository;
    private final PatientService patientService;
    private final ScheduleService scheduleService;
    private final AdherenceService adherenceService;
    private final ReminderSettingsService reminderSettingsService;
    private final PrescriptionService prescriptionService;

    public PatientViewController(AuthService authService,
                                 PatientRepository patientRepository,
                                 PatientService patientService,
                                 ScheduleService scheduleService,
                                 AdherenceService adherenceService,
                                 ReminderSettingsService reminderSettingsService,
                                 PrescriptionService prescriptionService) {
        this.authService = authService;
        this.patientRepository = patientRepository;
        this.patientService = patientService;
        this.scheduleService = scheduleService;
        this.adherenceService = adherenceService;
        this.reminderSettingsService = reminderSettingsService;
        this.prescriptionService = prescriptionService;
    }

    private Patient getCurrentPatient() {
        User user = authService.getRequiredCurrentUser();
        return patientRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Patient profile not found for user: " + user.getEmail()));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Patient patient = getCurrentPatient();
        PatientDto patientDto = patientService.convertToDto(patient);
        List<MedicineScheduleDto> todaySchedules = scheduleService.getTodaySchedule(patient.getId());
        Optional<MedicineScheduleDto> nextDose = scheduleService.getNextDose(patient.getId());
        AdherenceStatsDto stats = adherenceService.getPatientAdherenceStats(patient.getId());
        ReminderSettingsDto settings = reminderSettingsService.getSettings(patient.getId());

        model.addAttribute("patient", patientDto);
        model.addAttribute("todaySchedules", todaySchedules);
        model.addAttribute("nextDose", nextDose.orElse(null));
        model.addAttribute("stats", stats);
        model.addAttribute("settings", settings);
        return "patient/dashboard";
    }

    @GetMapping("/timetable")
    public String timetable(Model model) {
        Patient patient = getCurrentPatient();
        PatientDto patientDto = patientService.convertToDto(patient);
        List<MedicineScheduleDto> todaySchedules = scheduleService.getTodaySchedule(patient.getId());
        ReminderSettingsDto settings = reminderSettingsService.getSettings(patient.getId());

        model.addAttribute("patient", patientDto);
        model.addAttribute("todaySchedules", todaySchedules);
        model.addAttribute("todayDate", LocalDate.now().toString());
        model.addAttribute("settings", settings);
        return "patient/timetable";
    }

    @GetMapping("/history")
    public String history(Model model) {
        Patient patient = getCurrentPatient();
        PatientDto patientDto = patientService.convertToDto(patient);
        List<MedicineScheduleDto> history = scheduleService.getMedicationHistory(patient.getId());
        AdherenceStatsDto stats = adherenceService.getPatientAdherenceStats(patient.getId());

        model.addAttribute("patient", patientDto);
        model.addAttribute("history", history);
        model.addAttribute("stats", stats);
        return "patient/history";
    }

    @GetMapping("/statistics")
    public String statistics(Model model) {
        Patient patient = getCurrentPatient();
        PatientDto patientDto = patientService.convertToDto(patient);
        AdherenceStatsDto stats = adherenceService.getPatientAdherenceStats(patient.getId());

        model.addAttribute("patient", patientDto);
        model.addAttribute("stats", stats);
        return "patient/statistics";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        Patient patient = getCurrentPatient();
        PatientDto patientDto = patientService.convertToDto(patient);
        ReminderSettingsDto settings = reminderSettingsService.getSettings(patient.getId());

        model.addAttribute("patient", patientDto);
        model.addAttribute("settings", settings);
        return "patient/settings";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        Patient patient = getCurrentPatient();
        PatientDto patientDto = patientService.convertToDto(patient);
        List<PrescriptionResponseDto> prescriptions = prescriptionService.getPrescriptionsByPatient(patient.getId());

        model.addAttribute("patient", patientDto);
        model.addAttribute("prescriptions", prescriptions);
        return "patient/profile";
    }
}
