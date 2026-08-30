package com.medtime.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ScheduleTimeHelper {

    private static final DateTimeFormatter TIME_24_FORMAT = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_12_FORMAT = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);

    public static String formatTo12Hour(String time24) {
        if (time24 == null || time24.isBlank()) {
            return "";
        }
        try {
            LocalTime lt = LocalTime.parse(time24.trim(), TIME_24_FORMAT);
            return lt.format(TIME_12_FORMAT).toUpperCase(Locale.ENGLISH);
        } catch (Exception e) {
            return time24;
        }
    }

    public static List<String> getDefaultTimesForFrequency(String frequency) {
        List<String> times = new ArrayList<>();
        if (frequency == null) {
            times.add("08:00");
            return times;
        }

        String lower = frequency.toLowerCase(Locale.ENGLISH).trim();
        if (lower.contains("once") || lower.startsWith("1")) {
            times.add("08:00");
        } else if (lower.contains("twice") || lower.startsWith("2")) {
            times.add("08:00");
            times.add("20:00");
        } else if (lower.contains("3") || lower.contains("thrice")) {
            times.add("08:00");
            times.add("14:00");
            times.add("20:00");
        } else if (lower.contains("4")) {
            times.add("08:00");
            times.add("12:00");
            times.add("16:00");
            times.add("20:00");
        } else if (lower.contains("6 hours")) {
            times.add("06:00");
            times.add("12:00");
            times.add("18:00");
            times.add("00:00");
        } else if (lower.contains("8 hours")) {
            times.add("08:00");
            times.add("16:00");
            times.add("00:00");
        } else {
            times.add("08:00");
        }
        return times;
    }

    public static List<String> normalizeAndSortTimes(List<String> rawTimes) {
        if (rawTimes == null || rawTimes.isEmpty()) {
            return Collections.singletonList("08:00");
        }
        List<String> normalized = new ArrayList<>();
        for (String t : rawTimes) {
            if (t != null && !t.trim().isEmpty()) {
                String clean = t.trim();
                // Ensure HH:mm format
                if (clean.length() == 4 && clean.charAt(1) == ':') {
                    clean = "0" + clean;
                }
                if (!normalized.contains(clean)) {
                    normalized.add(clean);
                }
            }
        }
        Collections.sort(normalized);
        return normalized.isEmpty() ? Collections.singletonList("08:00") : normalized;
    }
}
