package com.medtime.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

public class MedicineDto {

    private Long id;

    @NotBlank(message = "Medicine name is required")
    private String medicineName;

    private String medicineType = "Tablet";

    @NotBlank(message = "Dosage is required (e.g. 500 mg)")
    private String dosage;

    @NotBlank(message = "Frequency is required (e.g. 3 times per day)")
    private String frequency;

    @Min(value = 1, message = "Duration must be at least 1 day")
    private int durationDays = 5;

    private String mealInstruction = "After food";

    private String specialInstruction;

    private List<String> reminderTimes = new ArrayList<>();

    public MedicineDto() {
    }

    public MedicineDto(String medicineName, String medicineType, String dosage, String frequency, int durationDays, String mealInstruction, String specialInstruction, List<String> reminderTimes) {
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
}
