package com.medtime.repository;

import com.medtime.entity.Doctor;
import com.medtime.entity.Patient;
import com.medtime.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    List<Prescription> findByDoctorOrderByPrescriptionDateDesc(Doctor doctor);
    List<Prescription> findByDoctorIdOrderByPrescriptionDateDesc(Long doctorId);
    List<Prescription> findByPatientOrderByPrescriptionDateDesc(Patient patient);
    List<Prescription> findByPatientIdOrderByPrescriptionDateDesc(Long patientId);
    List<Prescription> findAllByOrderByPrescriptionDateDesc();
}
