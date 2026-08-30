package com.medtime.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class ReminderSettingsDto {

    private boolean soundEnabled = true;

    @Min(value = 0, message = "Volume must be between 0 and 100")
    @Max(value = 100, message = "Volume must be between 0 and 100")
    private int volume = 80;

    @Min(value = 1, message = "Snooze must be at least 1 minute")
    @Max(value = 60, message = "Snooze cannot exceed 60 minutes")
    private int snoozeMinutes = 10;

    @NotBlank(message = "Alarm sound is required")
    private String alarmSound = "medical_beep";

    public ReminderSettingsDto() {
    }

    public ReminderSettingsDto(boolean soundEnabled, int volume, int snoozeMinutes, String alarmSound) {
        this.soundEnabled = soundEnabled;
        this.volume = volume;
        this.snoozeMinutes = snoozeMinutes;
        this.alarmSound = alarmSound;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public int getSnoozeMinutes() {
        return snoozeMinutes;
    }

    public void setSnoozeMinutes(int snoozeMinutes) {
        this.snoozeMinutes = snoozeMinutes;
    }

    public String getAlarmSound() {
        return alarmSound;
    }

    public void setAlarmSound(String alarmSound) {
        this.alarmSound = alarmSound;
    }
}
