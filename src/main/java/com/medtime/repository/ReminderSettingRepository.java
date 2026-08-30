package com.medtime.repository;

import com.medtime.entity.Patient;
import com.medtime.entity.ReminderSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReminderSettingRepository extends JpaRepository<ReminderSetting, Long> {
    Optional<ReminderSetting> findByPatient(Patient patient);
    Optional<ReminderSetting> findByPatientId(Long patientId);
}
