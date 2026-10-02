package com.medtime.service;

import com.medtime.dto.MedicineScheduleDto;
import com.medtime.entity.*;
import com.medtime.repository.MedicineScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleServiceTest {

    @Mock
    private MedicineScheduleRepository scheduleRepository;

    @InjectMocks
    private ScheduleService scheduleService;

    private MedicineSchedule sampleSchedule;
    private Patient samplePatient;
    private Medicine sampleMedicine;

    @BeforeEach
    void setUp() {
        User user = new User("Rahul", "rahul@test.com", "pass", Role.PATIENT, "123");
        samplePatient = new Patient(user, LocalDate.of(1995, 1, 1), "Male", "O+", "999", "Address");
        samplePatient.setId(1L);

        sampleMedicine = new Medicine("Paracetamol", "Tablet", "500 mg", "Once daily", 5, "After food", "Drink water", Collections.singletonList("08:00"));
        sampleMedicine.setId(10L);

        sampleSchedule = new MedicineSchedule(sampleMedicine, samplePatient, LocalDate.now(), "08:00");
        sampleSchedule.setId(100L);
    }

    @Test
    void testMarkTaken() {
        when(scheduleRepository.findById(100L)).thenReturn(Optional.of(sampleSchedule));
        when(scheduleRepository.save(any(MedicineSchedule.class))).thenAnswer(i -> i.getArgument(0));

        MedicineScheduleDto result = scheduleService.markTaken(100L);

        assertNotNull(result);
        assertEquals(ScheduleStatus.TAKEN, result.getStatus());
        assertNotNull(result.getTakenAt());
    }

    @Test
    void testMarkMissed() {
        when(scheduleRepository.findById(100L)).thenReturn(Optional.of(sampleSchedule));
        when(scheduleRepository.save(any(MedicineSchedule.class))).thenAnswer(i -> i.getArgument(0));

        MedicineScheduleDto result = scheduleService.markMissed(100L);

        assertNotNull(result);
        assertEquals(ScheduleStatus.MISSED, result.getStatus());
    }

    @Test
    void testSnoozeSchedule() {
        when(scheduleRepository.findById(100L)).thenReturn(Optional.of(sampleSchedule));
        when(scheduleRepository.save(any(MedicineSchedule.class))).thenAnswer(i -> i.getArgument(0));

        MedicineScheduleDto result = scheduleService.snoozeSchedule(100L, 15);

        assertNotNull(result);
        assertEquals(ScheduleStatus.SNOOZED, result.getStatus());
        assertNotNull(result.getSnoozedUntil());
        assertTrue(result.getSnoozedUntil().isAfter(LocalDateTime.now()));
    }

    @Test
    void testGetTodaySchedule() {
        when(scheduleRepository.findByPatientIdAndScheduledDateOrderByScheduledTimeAsc(eq(1L), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(sampleSchedule));

        List<MedicineScheduleDto> list = scheduleService.getTodaySchedule(1L);

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("08:00 AM", list.get(0).getScheduledTimeFormatted());
    }
}
