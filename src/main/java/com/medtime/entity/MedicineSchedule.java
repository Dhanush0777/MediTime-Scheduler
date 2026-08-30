package com.medtime.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medicine_schedules", indexes = {
    @Index(name = "idx_patient_date", columnList = "patient_id, scheduled_date"),
    @Index(name = "idx_patient_status", columnList = "patient_id, status")
})
public class MedicineSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time", nullable = false)
    private String scheduledTime; // e.g. "08:00", "14:00", "20:00" (24h format HH:mm)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduleStatus status = ScheduleStatus.PENDING;

    @Column(name = "taken_at")
    private LocalDateTime takenAt;

    @Column(name = "snoozed_until")
    private LocalDateTime snoozedUntil;

    @Column(name = "notes")
    private String notes;

    public MedicineSchedule() {
    }

    public MedicineSchedule(Medicine medicine, Patient patient, LocalDate scheduledDate, String scheduledTime) {
        this.medicine = medicine;
        this.patient = patient;
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
        this.status = ScheduleStatus.PENDING;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Medicine getMedicine() {
        return medicine;
    }

    public void setMedicine(Medicine medicine) {
        this.medicine = medicine;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public ScheduleStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduleStatus status) {
        this.status = status;
    }

    public LocalDateTime getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(LocalDateTime takenAt) {
        this.takenAt = takenAt;
    }

    public LocalDateTime getSnoozedUntil() {
        return snoozedUntil;
    }

    public void setSnoozedUntil(LocalDateTime snoozedUntil) {
        this.snoozedUntil = snoozedUntil;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
