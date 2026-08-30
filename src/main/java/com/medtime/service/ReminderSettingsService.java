package com.medtime.service;

import com.medtime.dto.ReminderSettingsDto;
import com.medtime.entity.Patient;
import com.medtime.entity.ReminderSetting;
import com.medtime.exception.ResourceNotFoundException;
import com.medtime.repository.PatientRepository;
import com.medtime.repository.ReminderSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReminderSettingsService {

    private final ReminderSettingRepository reminderSettingRepository;
    private final PatientRepository patientRepository;

    public ReminderSettingsService(ReminderSettingRepository reminderSettingRepository,
                                   PatientRepository patientRepository) {
        this.reminderSettingRepository = reminderSettingRepository;
        this.patientRepository = patientRepository;
    }

    public ReminderSettingsDto getSettings(Long patientId) {
        ReminderSetting setting = reminderSettingRepository.findByPatientId(patientId)
                .orElseGet(() -> {
                    Patient p = patientRepository.findById(patientId)
                            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
                    ReminderSetting newSetting = new ReminderSetting(p);
                    return reminderSettingRepository.save(newSetting);
                });

        return new ReminderSettingsDto(
                setting.isSoundEnabled(),
                setting.getVolume(),
                setting.getSnoozeMinutes(),
                setting.getAlarmSound()
        );
    }

    @Transactional
    public ReminderSettingsDto updateSettings(Long patientId, ReminderSettingsDto request) {
        ReminderSetting setting = reminderSettingRepository.findByPatientId(patientId)
                .orElseGet(() -> {
                    Patient p = patientRepository.findById(patientId)
                            .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
                    return new ReminderSetting(p);
                });

        setting.setSoundEnabled(request.isSoundEnabled());
        setting.setVolume(request.getVolume());
        setting.setSnoozeMinutes(request.getSnoozeMinutes());
        if (request.getAlarmSound() != null && !request.getAlarmSound().isBlank()) {
            setting.setAlarmSound(request.getAlarmSound().trim());
        }

        ReminderSetting saved = reminderSettingRepository.save(setting);
        return new ReminderSettingsDto(
                saved.isSoundEnabled(),
                saved.getVolume(),
                saved.getSnoozeMinutes(),
                saved.getAlarmSound()
        );
    }
}
