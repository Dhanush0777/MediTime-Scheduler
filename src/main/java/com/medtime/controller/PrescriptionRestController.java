package com.medtime.controller;

import com.medtime.dto.ApiResponse;
import com.medtime.dto.PrescriptionRequest;
import com.medtime.dto.PrescriptionResponseDto;
import com.medtime.entity.Doctor;
import com.medtime.entity.User;
import com.medtime.repository.DoctorRepository;
import com.medtime.service.AuthService;
import com.medtime.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionRestController {

    private final PrescriptionService prescriptionService;
    private final AuthService authService;
    private final DoctorRepository doctorRepository;

    public PrescriptionRestController(PrescriptionService prescriptionService,
                                      AuthService authService,
                                      DoctorRepository doctorRepository) {
        this.prescriptionService = prescriptionService;
        this.authService = authService;
        this.doctorRepository = doctorRepository;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> createPrescription(
            @Valid @RequestBody PrescriptionRequest request) {

        Long doctorId = request.getDoctorId();
        Optional<User> currentUser = authService.getCurrentAuthenticatedUser();
        if (currentUser.isPresent()) {
            Optional<Doctor> doctorOpt = doctorRepository.findByUser(currentUser.get());
            if (doctorOpt.isPresent()) {
                doctorId = doctorOpt.get().getId();
            }
        }

        PrescriptionResponseDto created = prescriptionService.createPrescription(request, doctorId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Prescription and medicine timetable created successfully", created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PrescriptionResponseDto>> getPrescriptionById(@PathVariable Long id) {
        PrescriptionResponseDto prescription = prescriptionService.getPrescriptionDtoById(id);
        return ResponseEntity.ok(ApiResponse.ok("Prescription details", prescription));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDto>>> getAllPrescriptions() {
        List<PrescriptionResponseDto> list = prescriptionService.getAllPrescriptions();
        return ResponseEntity.ok(ApiResponse.ok("Prescription list", list));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDto>>> getPrescriptionsByPatient(@PathVariable Long patientId) {
        List<PrescriptionResponseDto> list = prescriptionService.getPrescriptionsByPatient(patientId);
        return ResponseEntity.ok(ApiResponse.ok("Patient prescriptions", list));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<ApiResponse<List<PrescriptionResponseDto>>> getPrescriptionsByDoctor(@PathVariable Long doctorId) {
        List<PrescriptionResponseDto> list = prescriptionService.getPrescriptionsByDoctor(doctorId);
        return ResponseEntity.ok(ApiResponse.ok("Doctor prescriptions", list));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePrescription(@PathVariable Long id) {
        prescriptionService.deletePrescription(id);
        return ResponseEntity.ok(ApiResponse.ok("Prescription deleted successfully"));
    }
}
