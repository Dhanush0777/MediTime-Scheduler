package com.medtime.dto;

import jakarta.validation.constraints.Min;

public class SnoozeRequest {

    @Min(value = 1, message = "Snooze duration must be at least 1 minute")
    private int minutes = 10;

    public SnoozeRequest() {
    }

    public SnoozeRequest(int minutes) {
        this.minutes = minutes;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }
}
