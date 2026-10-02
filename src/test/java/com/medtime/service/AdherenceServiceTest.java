package com.medtime.service;

import com.medtime.dto.AdherenceStatsDto;
import com.medtime.entity.ScheduleStatus;
import com.medtime.repository.MedicineScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AdherenceServiceTest {

    @Mock
    private MedicineScheduleRepository scheduleRepository;

    @InjectMocks
    private AdherenceService adherenceService;

    @Test
    void testGetPatientAdherenceStatsCalculatesPercentages() {
        LocalDate today = LocalDate.now();

        when(scheduleRepository.countByPatientIdAndScheduledDate(1L, today)).thenReturn(4L);
        when(scheduleRepository.countByPatientIdAndScheduledDateAndStatus(1L, today, ScheduleStatus.TAKEN)).thenReturn(3L);
        when(scheduleRepository.countByPatientIdAndScheduledDateAndStatus(1L, today, ScheduleStatus.MISSED)).thenReturn(1L);
        when(scheduleRepository.countByPatientIdAndScheduledDateAndStatus(1L, today, ScheduleStatus.PENDING)).thenReturn(0L);
        when(scheduleRepository.countByPatientIdAndScheduledDateAndStatus(1L, today, ScheduleStatus.SNOOZED)).thenReturn(0L);

        when(scheduleRepository.countByPatientId(1L)).thenReturn(20L);
        when(scheduleRepository.countByPatientIdAndStatus(1L, ScheduleStatus.TAKEN)).thenReturn(18L);
        when(scheduleRepository.countByPatientIdAndStatus(1L, ScheduleStatus.MISSED)).thenReturn(2L);
        when(scheduleRepository.countByPatientIdAndStatus(1L, ScheduleStatus.PENDING)).thenReturn(0L);
        when(scheduleRepository.countByPatientIdAndStatus(1L, ScheduleStatus.SNOOZED)).thenReturn(0L);

        when(scheduleRepository.findByPatientIdAndScheduledDateBetweenOrderByScheduledDateAscScheduledTimeAsc(
                eq(1L), any(LocalDate.class), eq(today))).thenReturn(Collections.emptyList());

        AdherenceStatsDto stats = adherenceService.getPatientAdherenceStats(1L);

        assertNotNull(stats);
        assertEquals(4, stats.getTodayTotal());
        assertEquals(3, stats.getTodayTaken());
        assertEquals(75.0, stats.getTodayAdherencePercentage()); // (3/4)*100 = 75%
        assertEquals(90.0, stats.getOverallAdherencePercentage()); // (18/20)*100 = 90%
        assertEquals(7, stats.getRecentDays().size());
    }
}
