package com.medtime.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "reminder_settings")
public class ReminderSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", referencedColumnName = "id", nullable = false, unique = true)
    private Patient patient;

    @Column(name = "sound_enabled", nullable = false)
    private boolean soundEnabled = true;

    @Column(name = "volume", nullable = false)
    private int volume = 80; // 0 to 100

    @Column(name = "snooze_minutes", nullable = false)
    private int snoozeMinutes = 10; // 5, 10, 15

    @Column(name = "alarm_sound", nullable = false)
    private String alarmSound = "medical_beep"; // medical_beep, gentle_chime, pulse_siren, urgent_alert

    public ReminderSetting() {
    }

    public ReminderSetting(Patient patient) {
        this.patient = patient;
        this.soundEnabled = true;
        this.volume = 80;
        this.snoozeMinutes = 10;
        this.alarmSound = "medical_beep";
    }

    public ReminderSetting(Patient patient, boolean soundEnabled, int volume, int snoozeMinutes, String alarmSound) {
        this.patient = patient;
        this.soundEnabled = soundEnabled;
        this.volume = volume;
        this.snoozeMinutes = snoozeMinutes;
        this.alarmSound = alarmSound;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
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
