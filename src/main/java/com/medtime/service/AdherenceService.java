package com.medtime.service;

import com.medtime.dto.AdherenceStatsDto;
import com.medtime.dto.AdherenceStatsDto.DailyAdherenceDto;
import com.medtime.entity.MedicineSchedule;
import com.medtime.entity.ScheduleStatus;
import com.medtime.repository.MedicineScheduleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdherenceService {

    private final MedicineScheduleRepository scheduleRepository;
    private static final DateTimeFormatter DAY_LABEL_FORMAT = DateTimeFormatter.ofPattern("EEE, MMM d");

    public AdherenceService(MedicineScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public AdherenceStatsDto getPatientAdherenceStats(Long patientId) {
        LocalDate today = LocalDate.now();

        // Today's statistics
        long todayTotal = scheduleRepository.countByPatientIdAndScheduledDate(patientId, today);
        long todayTaken = scheduleRepository.countByPatientIdAndScheduledDateAndStatus(patientId, today, ScheduleStatus.TAKEN);
        long todayMissed = scheduleRepository.countByPatientIdAndScheduledDateAndStatus(patientId, today, ScheduleStatus.MISSED);
        long todayPending = scheduleRepository.countByPatientIdAndScheduledDateAndStatus(patientId, today, ScheduleStatus.PENDING)
                + scheduleRepository.countByPatientIdAndScheduledDateAndStatus(patientId, today, ScheduleStatus.SNOOZED);

        double todayAdherence = todayTotal > 0 ? ((double) todayTaken / todayTotal) * 100.0 : 100.0;

        // Overall statistics
        long overallTotal = scheduleRepository.countByPatientId(patientId);
        long overallTaken = scheduleRepository.countByPatientIdAndStatus(patientId, ScheduleStatus.TAKEN);
        long overallMissed = scheduleRepository.countByPatientIdAndStatus(patientId, ScheduleStatus.MISSED);
        long overallPending = scheduleRepository.countByPatientIdAndStatus(patientId, ScheduleStatus.PENDING)
                + scheduleRepository.countByPatientIdAndStatus(patientId, ScheduleStatus.SNOOZED);

        double overallAdherence = overallTotal > 0 ? ((double) overallTaken / overallTotal) * 100.0 : 100.0;

        // 7-day trend
        List<DailyAdherenceDto> recentDays = new ArrayList<>();
        LocalDate startDate = today.minusDays(6);
        List<MedicineSchedule> recentSchedules = scheduleRepository
                .findByPatientIdAndScheduledDateBetweenOrderByScheduledDateAscScheduledTimeAsc(patientId, startDate, today);

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long dayTotal = recentSchedules.stream().filter(s -> s.getScheduledDate().isEqual(date)).count();
            long dayTaken = recentSchedules.stream().filter(s -> s.getScheduledDate().isEqual(date) && s.getStatus() == ScheduleStatus.TAKEN).count();
            long dayMissed = recentSchedules.stream().filter(s -> s.getScheduledDate().isEqual(date) && s.getStatus() == ScheduleStatus.MISSED).count();
            long dayPending = recentSchedules.stream().filter(s -> s.getScheduledDate().isEqual(date) && (s.getStatus() == ScheduleStatus.PENDING || s.getStatus() == ScheduleStatus.SNOOZED)).count();
            double dayPercentage = dayTotal > 0 ? ((double) dayTaken / dayTotal) * 100.0 : 100.0;

            recentDays.add(new DailyAdherenceDto(
                    date,
                    date.format(DAY_LABEL_FORMAT),
                    dayTotal,
                    dayTaken,
                    dayMissed,
                    dayPending,
                    Math.round(dayPercentage * 10.0) / 10.0
            ));
        }

        AdherenceStatsDto dto = new AdherenceStatsDto();
        dto.setTodayTotal(todayTotal);
        dto.setTodayTaken(todayTaken);
        dto.setTodayMissed(todayMissed);
        dto.setTodayPending(todayPending);
        dto.setTodayAdherencePercentage(Math.round(todayAdherence * 10.0) / 10.0);

        dto.setOverallTotal(overallTotal);
        dto.setOverallTaken(overallTaken);
        dto.setOverallMissed(overallMissed);
        dto.setOverallPending(overallPending);
        dto.setOverallAdherencePercentage(Math.round(overallAdherence * 10.0) / 10.0);

        dto.setRecentDays(recentDays);

        return dto;
    }
}
