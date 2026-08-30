package com.medtime.repository;

import com.medtime.entity.MedicineSchedule;
import com.medtime.entity.Patient;
import com.medtime.entity.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineScheduleRepository extends JpaRepository<MedicineSchedule, Long> {

    List<MedicineSchedule> findByPatientAndScheduledDateOrderByScheduledTimeAsc(Patient patient, LocalDate scheduledDate);

    List<MedicineSchedule> findByPatientIdAndScheduledDateOrderByScheduledTimeAsc(Long patientId, LocalDate scheduledDate);

    List<MedicineSchedule> findByPatientIdOrderByScheduledDateDescScheduledTimeDesc(Long patientId);

    List<MedicineSchedule> findByPatientIdAndScheduledDateBetweenOrderByScheduledDateAscScheduledTimeAsc(
            Long patientId, LocalDate startDate, LocalDate endDate);

    long countByPatientIdAndScheduledDate(Long patientId, LocalDate scheduledDate);

    long countByPatientIdAndScheduledDateAndStatus(Long patientId, LocalDate scheduledDate, ScheduleStatus status);

    long countByPatientIdAndStatus(Long patientId, ScheduleStatus status);

    long countByPatientId(Long patientId);

    @Modifying
    @Query("DELETE FROM MedicineSchedule ms WHERE ms.medicine.prescription.id = :prescriptionId")
    void deleteByPrescriptionId(@Param("prescriptionId") Long prescriptionId);
}
