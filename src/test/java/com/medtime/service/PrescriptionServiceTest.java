package com.medtime.service;

import com.medtime.dto.MedicineDto;
import com.medtime.dto.PrescriptionRequest;
import com.medtime.dto.PrescriptionResponseDto;
import com.medtime.entity.*;
import com.medtime.exception.BadRequestException;
import com.medtime.repository.DoctorRepository;
import com.medtime.repository.MedicineRepository;
import com.medtime.repository.MedicineScheduleRepository;
import com.medtime.repository.PatientRepository;
import com.medtime.repository.PrescriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrescriptionServiceTest {

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private MedicineScheduleRepository scheduleRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Doctor sampleDoctor;
    private Patient samplePatient;
    private User docUser;
    private User patientUser;

    @BeforeEach
    void setUp() {
        docUser = new User("Dr. Arun", "arun@test.com", "pass", Role.DOCTOR, "111");
        docUser.setId(1L);
        sampleDoctor = new Doctor(docUser, "General Medicine", "LIC-123", "Hospital", "MD");
        sampleDoctor.setId(10L);

        patientUser = new User("Rahul", "rahul@test.com", "pass", Role.PATIENT, "222");
        patientUser.setId(2L);
        samplePatient = new Patient(patientUser, LocalDate.of(1996, 1, 1), "Male", "O+", "999", "Address");
        samplePatient.setId(20L);
    }

    @Test
    void testCreatePrescriptionGeneratesTimetable() {
        LocalDate startDate = LocalDate.of(2026, 8, 30);
        LocalDate endDate = LocalDate.of(2026, 9, 1); // 3 days

        PrescriptionRequest req = new PrescriptionRequest();
        req.setPatientId(20L);
        req.setDoctorId(10L);
        req.setStartDate(startDate);
        req.setEndDate(endDate);
        req.setDiagnosis("Fever");

        MedicineDto med1 = new MedicineDto(
                "Paracetamol", "Tablet", "500 mg", "3 times per day", 3, "After food", null, Arrays.asList("08:00", "14:00", "20:00")
        );
        req.setMedicines(Collections.singletonList(med1));

        when(doctorRepository.findById(10L)).thenReturn(Optional.of(sampleDoctor));
        when(patientRepository.findById(20L)).thenReturn(Optional.of(samplePatient));

        Prescription savedRx = new Prescription(sampleDoctor, samplePatient, LocalDate.now(), startDate, endDate, "Fever", null);
        savedRx.setId(100L);
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(savedRx);

        Medicine savedMed = new Medicine("Paracetamol", "Tablet", "500 mg", "3 times per day", 3, "After food", null, Arrays.asList("08:00", "14:00", "20:00"));
        savedMed.setId(50L);
        savedMed.setPrescription(savedRx);
        when(medicineRepository.save(any(Medicine.class))).thenReturn(savedMed);

        when(prescriptionRepository.findById(100L)).thenReturn(Optional.of(savedRx));
        when(medicineRepository.findByPrescriptionId(100L)).thenReturn(Collections.singletonList(savedMed));

        PrescriptionResponseDto response = prescriptionService.createPrescription(req, 10L);

        assertNotNull(response);
        assertEquals(100L, response.getId());

        // Verify that 3 days x 3 times = 9 schedule entries are saved
        ArgumentCaptor<List<MedicineSchedule>> captor = ArgumentCaptor.forClass(List.class);
        verify(scheduleRepository).saveAll(captor.capture());
        List<MedicineSchedule> savedSchedules = captor.getValue();
        assertEquals(9, savedSchedules.size());
        assertEquals("08:00", savedSchedules.get(0).getScheduledTime());
    }

    @Test
    void testCreatePrescriptionInvalidDateThrowsBadRequest() {
        PrescriptionRequest req = new PrescriptionRequest();
        req.setStartDate(LocalDate.of(2026, 9, 5));
        req.setEndDate(LocalDate.of(2026, 9, 1)); // Invalid: end < start

        assertThrows(BadRequestException.class, () -> prescriptionService.createPrescription(req, 10L));
    }
}
