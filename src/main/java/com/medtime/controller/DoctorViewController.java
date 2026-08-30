package com.medtime.controller;

import com.medtime.dto.DoctorDto;
import com.medtime.dto.PatientDto;
import com.medtime.dto.PrescriptionResponseDto;
import com.medtime.entity.Doctor;
import com.medtime.entity.User;
import com.medtime.repository.DoctorRepository;
import com.medtime.service.AuthService;
import com.medtime.service.DoctorService;
import com.medtime.service.PatientService;
import com.medtime.service.PrescriptionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorViewController {

    private final AuthService authService;
    private final DoctorRepository doctorRepository;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final PrescriptionService prescriptionService;

    public DoctorViewController(AuthService authService,
                                DoctorRepository doctorRepository,
                                DoctorService doctorService,
                                PatientService patientService,
                                PrescriptionService prescriptionService) {
        this.authService = authService;
        this.doctorRepository = doctorRepository;
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.prescriptionService = prescriptionService;
    }

    private Doctor getCurrentDoctor() {
        User user = authService.getRequiredCurrentUser();
        return doctorRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Doctor profile not found for user: " + user.getEmail()));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Doctor doctor = getCurrentDoctor();
        DoctorDto docDto = doctorService.convertToDto(doctor);
        List<PatientDto> patients = patientService.getAllPatients();
        List<PrescriptionResponseDto> prescriptions = prescriptionService.getPrescriptionsByDoctor(doctor.getId());

        model.addAttribute("doctor", docDto);
        model.addAttribute("patients", patients);
        model.addAttribute("prescriptions", prescriptions);
        model.addAttribute("totalPatients", patients.size());
        model.addAttribute("totalPrescriptions", prescriptions.size());
        return "doctor/dashboard";
    }

    @GetMapping("/patients")
    public String patients(Model model) {
        Doctor doctor = getCurrentDoctor();
        model.addAttribute("doctor", doctorService.convertToDto(doctor));
        model.addAttribute("patients", patientService.getAllPatients());
        return "doctor/patients";
    }

    @GetMapping("/patients/{id}/history")
    public String patientHistory(@PathVariable Long id, Model model) {
        Doctor doctor = getCurrentDoctor();
        PatientDto patient = patientService.getPatientDtoById(id);
        List<PrescriptionResponseDto> prescriptions = prescriptionService.getPrescriptionsByPatient(id);

        model.addAttribute("doctor", doctorService.convertToDto(doctor));
        model.addAttribute("patient", patient);
        model.addAttribute("prescriptions", prescriptions);
        return "doctor/patient-history";
    }

    @GetMapping("/prescriptions")
    public String prescriptions(Model model) {
        Doctor doctor = getCurrentDoctor();
        List<PrescriptionResponseDto> prescriptions = prescriptionService.getPrescriptionsByDoctor(doctor.getId());
        model.addAttribute("doctor", doctorService.convertToDto(doctor));
        model.addAttribute("prescriptions", prescriptions);
        return "doctor/prescriptions";
    }

    @GetMapping("/prescriptions/new")
    public String newPrescription(Model model) {
        Doctor doctor = getCurrentDoctor();
        List<PatientDto> patients = patientService.getAllPatients();
        model.addAttribute("doctor", doctorService.convertToDto(doctor));
        model.addAttribute("patients", patients);
        return "doctor/create-prescription";
    }

    @GetMapping("/prescriptions/{id}")
    public String viewPrescription(@PathVariable Long id, Model model) {
        Doctor doctor = getCurrentDoctor();
        PrescriptionResponseDto prescription = prescriptionService.getPrescriptionDtoById(id);
        model.addAttribute("doctor", doctorService.convertToDto(doctor));
        model.addAttribute("prescription", prescription);
        return "doctor/prescription-detail";
    }
}
