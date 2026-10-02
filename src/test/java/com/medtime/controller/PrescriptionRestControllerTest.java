package com.medtime.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medtime.dto.MedicineDto;
import com.medtime.dto.PrescriptionRequest;
import com.medtime.dto.PrescriptionResponseDto;
import com.medtime.repository.DoctorRepository;
import com.medtime.security.CustomUserDetailsService;
import com.medtime.service.AuthService;
import com.medtime.service.PrescriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PrescriptionRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PrescriptionRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PrescriptionService prescriptionService;

    @MockBean
    private AuthService authService;

    @MockBean
    private DoctorRepository doctorRepository;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "dr.arun@meditime.com", roles = {"DOCTOR"})
    void testCreatePrescriptionApi() throws Exception {
        PrescriptionRequest req = new PrescriptionRequest();
        req.setPatientId(1L);
        req.setDoctorId(1L);
        req.setStartDate(LocalDate.of(2026, 8, 30));
        req.setEndDate(LocalDate.of(2026, 9, 3));
        req.setDiagnosis("Common Cold");

        MedicineDto med = new MedicineDto("Paracetamol", "Tablet", "500 mg", "3 times per day", 5, "After food", null, Arrays.asList("08:00", "14:00", "20:00"));
        req.setMedicines(Collections.singletonList(med));

        PrescriptionResponseDto responseDto = new PrescriptionResponseDto();
        responseDto.setId(10L);
        responseDto.setDiagnosis("Common Cold");

        when(prescriptionService.createPrescription(any(PrescriptionRequest.class), any())).thenReturn(responseDto);

        mockMvc.perform(post("/api/prescriptions")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.diagnosis").value("Common Cold"));
    }

    @Test
    @WithMockUser(username = "dr.arun@meditime.com", roles = {"DOCTOR"})
    void testGetPrescriptionByIdApi() throws Exception {
        PrescriptionResponseDto responseDto = new PrescriptionResponseDto();
        responseDto.setId(10L);
        responseDto.setDiagnosis("Fever");

        when(prescriptionService.getPrescriptionDtoById(10L)).thenReturn(responseDto);

        mockMvc.perform(get("/api/prescriptions/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(10));
    }
}
