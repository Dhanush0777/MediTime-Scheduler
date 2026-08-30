package com.medtime.config;

import com.medtime.dto.MedicineDto;
import com.medtime.dto.PrescriptionRequest;
import com.medtime.dto.RegisterRequest;
import com.medtime.entity.MedicineSchedule;
import com.medtime.entity.Role;
import com.medtime.entity.ScheduleStatus;
import com.medtime.repository.MedicineScheduleRepository;
import com.medtime.repository.UserRepository;
import com.medtime.service.AuthService;
import com.medtime.service.PrescriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AuthService authService;
    private final PrescriptionService prescriptionService;
    private final MedicineScheduleRepository scheduleRepository;

    public DataInitializer(UserRepository userRepository,
                           AuthService authService,
                           PrescriptionService prescriptionService,
                           MedicineScheduleRepository scheduleRepository) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.prescriptionService = prescriptionService;
        this.scheduleRepository = scheduleRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains data, skipping initialization.");
            return;
        }

        log.info("Seeding initial demo data for MediTime application...");

        // 1. Create Doctor: Dr. Arun
        RegisterRequest docReq = new RegisterRequest();
        docReq.setName("Dr. Arun Verma");
        docReq.setEmail("dr.arun@meditime.com");
        docReq.setPassword("password123");
        docReq.setRole(Role.DOCTOR);
        docReq.setPhone("+91 98765 43210");
        docReq.setSpecialization("Cardiologist & General Medicine");
        docReq.setLicenseNumber("MED-88902");
        docReq.setHospitalAffiliation("Apollo Health Institute");
        docReq.setQualification("MBBS, MD (General Medicine)");
        var docAuth = authService.register(docReq);

        // 2. Create Patient 1: Rahul Sharma
        RegisterRequest patientReq1 = new RegisterRequest();
        patientReq1.setName("Rahul Sharma");
        patientReq1.setEmail("rahul@meditime.com");
        patientReq1.setPassword("password123");
        patientReq1.setRole(Role.PATIENT);
        patientReq1.setPhone("+91 98765 12345");
        patientReq1.setDateOfBirth(LocalDate.of(1996, 5, 14));
        patientReq1.setGender("Male");
        patientReq1.setBloodGroup("B+");
        patientReq1.setEmergencyContact("+91 98765 99999");
        patientReq1.setAddress("42 MG Road, Indiranagar, Bengaluru");
        var patientAuth1 = authService.register(patientReq1);

        // 3. Create Patient 2: Priya Patel
        RegisterRequest patientReq2 = new RegisterRequest();
        patientReq2.setName("Priya Patel");
        patientReq2.setEmail("priya@meditime.com");
        patientReq2.setPassword("password123");
        patientReq2.setRole(Role.PATIENT);
        patientReq2.setPhone("+91 91234 56780");
        patientReq2.setDateOfBirth(LocalDate.of(1999, 8, 22));
        patientReq2.setGender("Female");
        patientReq2.setBloodGroup("O+");
        patientReq2.setEmergencyContact("+91 91234 11111");
        patientReq2.setAddress("18 Residency Road, Richmond Town, Bengaluru");
        var patientAuth2 = authService.register(patientReq2);

        // 4. Create Prescription for Rahul
        LocalDate today = LocalDate.now();
        PrescriptionRequest rx1 = new PrescriptionRequest();
        rx1.setPatientId(patientAuth1.getProfileId());
        rx1.setDoctorId(docAuth.getProfileId());
        rx1.setPrescriptionDate(today.minusDays(2));
        rx1.setStartDate(today.minusDays(2));
        rx1.setEndDate(today.plusDays(5));
        rx1.setDiagnosis("Upper Respiratory Tract Infection & Mild Pyrexia");
        rx1.setNotes("Take medicines strictly on schedule with warm water. Stay hydrated and avoid cold beverages.");

        MedicineDto med1 = new MedicineDto(
                "Paracetamol",
                "Tablet",
                "500 mg",
                "3 times per day",
                7,
                "After food",
                "Take with a full glass of water",
                Arrays.asList("08:00", "14:00", "20:00")
        );

        MedicineDto med2 = new MedicineDto(
                "Amoxicillin",
                "Capsule",
                "500 mg",
                "2 times per day",
                7,
                "After food",
                "Complete the full antibiotic course",
                Arrays.asList("09:00", "21:00")
        );

        MedicineDto med3 = new MedicineDto(
                "Vitamin D3",
                "Tablet",
                "60000 IU",
                "Once daily",
                7,
                "With food",
                "Take with milk or fatty meal",
                Arrays.asList("10:00")
        );

        rx1.setMedicines(Arrays.asList(med1, med2, med3));
        prescriptionService.createPrescription(rx1, docAuth.getProfileId());

        // Update past schedules for realistic adherence history
        List<MedicineSchedule> pastSchedules = scheduleRepository.findByPatientIdAndScheduledDateBetweenOrderByScheduledDateAscScheduledTimeAsc(
                patientAuth1.getProfileId(), today.minusDays(2), today.minusDays(1));

        int count = 0;
        for (MedicineSchedule s : pastSchedules) {
            if (count % 5 == 3) {
                s.setStatus(ScheduleStatus.MISSED);
            } else {
                s.setStatus(ScheduleStatus.TAKEN);
                s.setTakenAt(s.getScheduledDate().atTime(8, 5));
            }
            count++;
        }
        scheduleRepository.saveAll(pastSchedules);

        log.info("Demo data seeded successfully!");
        log.info("Doctor Login: dr.arun@meditime.com / password123");
        log.info("Patient Login: rahul@meditime.com / password123");
    }
}
