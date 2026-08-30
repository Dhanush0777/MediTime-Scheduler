package com.medtime.controller;

import com.medtime.dto.ApiResponse;
import com.medtime.dto.DoctorDto;
import com.medtime.service.DoctorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorRestController {

    private final DoctorService doctorService;

    public DoctorRestController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<DoctorDto>>> getAllDoctors() {
        List<DoctorDto> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(ApiResponse.ok("Doctors list", doctors));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DoctorDto>> getDoctorById(@PathVariable Long id) {
        DoctorDto doctor = doctorService.getDoctorDtoById(id);
        return ResponseEntity.ok(ApiResponse.ok("Doctor profile", doctor));
    }
}
