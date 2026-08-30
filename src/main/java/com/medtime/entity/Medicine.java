package com.medtime.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medicines")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    @Column(name = "medicine_name", nullable = false)
    private String medicineName;

    @Column(name = "medicine_type")
    private String medicineType = "Tablet"; // Tablet, Capsule, Syrup, Drops, Injection, Inhaler, Ointment

    @Column(name = "dosage", nullable = false)
    private String dosage; // e.g. "500 mg", "10 ml", "1 tablet"

    @Column(name = "frequency", nullable = false)
    private String frequency; // e.g. "3 times per day", "Once daily", "Twice daily"

    @Column(name = "duration_days", nullable = false)
    private int durationDays;

    @Column(name = "meal_instruction")
    private String mealInstruction = "After food"; // Before food, After food, With food, Empty stomach, No relation

    @Column(name = "special_instruction", length = 500)
    private String specialInstruction;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "medicine_reminder_times", joinColumns = @JoinColumn(name = "medicine_id"))
    @Column(name = "reminder_time")
    private List<String> reminderTimes = new ArrayList<>(); // e.g., ["08:00", "14:00", "20:00"]

    @JsonIgnore
    @OneToMany(mappedBy = "medicine", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<MedicineSchedule> schedules = new ArrayList<>();

    public Medicine() {
    }

    public Medicine(String medicineName, String medicineType, String dosage, String frequency, int durationDays, String mealInstruction, String specialInstruction, List<String> reminderTimes) {
        this.medicineName = medicineName;
        this.medicineType = medicineType;
        this.dosage = dosage;
        this.frequency = frequency;
        this.durationDays = durationDays;
        this.mealInstruction = mealInstruction;
        this.specialInstruction = specialInstruction;
        this.reminderTimes = reminderTimes != null ? reminderTimes : new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getMedicineType() {
        return medicineType;
    }

    public void setMedicineType(String medicineType) {
        this.medicineType = medicineType;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public String getMealInstruction() {
        return mealInstruction;
    }

    public void setMealInstruction(String mealInstruction) {
        this.mealInstruction = mealInstruction;
    }

    public String getSpecialInstruction() {
        return specialInstruction;
    }

    public void setSpecialInstruction(String specialInstruction) {
        this.specialInstruction = specialInstruction;
    }

    public List<String> getReminderTimes() {
        return reminderTimes;
    }

    public void setReminderTimes(List<String> reminderTimes) {
        this.reminderTimes = reminderTimes;
    }

    public List<MedicineSchedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<MedicineSchedule> schedules) {
        this.schedules = schedules;
    }
}
