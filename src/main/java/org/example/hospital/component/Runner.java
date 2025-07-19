package org.example.hospital.component;

import lombok.RequiredArgsConstructor;
import org.example.hospital.entity.*;
import org.example.hospital.entity.enums.AdmissionStatus;
import org.example.hospital.entity.enums.ProcedureStatus;
import org.example.hospital.entity.users.*;
import org.example.hospital.repo.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Runner implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AdmissionRepository admissionRepository;
    private final ProcedureRepository procedureRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final AdministratorRepository administratorRepository;
    private final SpecialityRepository specialityRepository;

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddl;

    @Override
    public void run(String... args) throws Exception {
        if (!"create".equals(ddl)) {
            return;
        }

        // Create Roles
        Role adminRole = roleRepository.save(Role.builder()
                .name("ROLE_ADMIN")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        Role doctorRole = roleRepository.save(Role.builder()
                .name("ROLE_DOCTOR")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        Role patientRole = roleRepository.save(Role.builder()
                .name("ROLE_PATIENT")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        Role superAdminRole = roleRepository.save(Role.builder()
                .name("ROLE_SUPER_ADMIN")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // Create Specialities
        Speciality stomatology = specialityRepository.save(Speciality.builder()
                .name("Stomatology")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        Speciality cardiology = specialityRepository.save(Speciality.builder()
                .name("Cardiology")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // Create Admin User
        User adminUser = userRepository.save(User.builder()
                .firstName("Salmon")
                .lastName("Abdusattorov")
                .phone("+998912366533")
                .password(passwordEncoder.encode("123"))
                .birthDate(LocalDate.of(1985, 3, 20))
                .address("123 Chilanzar, Tashkent, Uzbekistan")
                .roles(List.of(adminRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        Administrator admin = administratorRepository.save(Administrator.builder()
                .user(adminUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // Create Super Admin User
        User superAdminUser = userRepository.save(User.builder()
                .firstName("Super")
                .lastName("Adminov")
                .phone("+998901234567")
                .password(passwordEncoder.encode("123"))
                .birthDate(LocalDate.of(1978, 7, 15))
                .address("456 Yunusabad, Tashkent, Uzbekistan")
                .roles(List.of(superAdminRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        administratorRepository.save(Administrator.builder()
                .user(superAdminUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // Create Doctor Users
        User doctorUser1 = userRepository.save(User.builder()
                .firstName("Hikmat")
                .lastName("Stamatolog")
                .phone("+998911234567")
                .password(passwordEncoder.encode("d"))
                .birthDate(LocalDate.of(1980, 5, 10))
                .address("789 Mirzo Ulugbek, Tashkent, Uzbekistan")
                .roles(List.of(doctorRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        User doctorUser2 = userRepository.save(User.builder()
                .firstName("Olga")
                .lastName("Ivanova")
                .phone("+998912345678")
                .password(passwordEncoder.encode("dd"))
                .birthDate(LocalDate.of(1975, 11, 25))
                .address("101 Sergeli, Tashkent, Uzbekistan")
                .roles(List.of(doctorRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        Doctor doctor1 = doctorRepository.save(Doctor.builder()
                .user(doctorUser1)
                .speciality(stomatology)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        Doctor doctor2 = doctorRepository.save(Doctor.builder()
                .user(doctorUser2)
                .speciality(cardiology)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // Create Patient Users
        User patientUser1 = userRepository.save(User.builder()
                .firstName("Eshmat")
                .lastName("Toshmatov")
                .phone("+998335750609")
                .password(passwordEncoder.encode("p"))
                .birthDate(LocalDate.of(1990, 8, 30))
                .address("202 Shaykhantahur, Tashkent, Uzbekistan")
                .roles(List.of(patientRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        User patientUser2 = userRepository.save(User.builder()
                .firstName("Ali")
                .lastName("Valiyev")
                .phone("+998335750610")
                .password(passwordEncoder.encode("pp"))
                .birthDate(LocalDate.of(1995, 2, 14))
                .address("303 Chilanzar, Tashkent, Uzbekistan")
                .roles(List.of(patientRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        Patient patient1 = patientRepository.save(Patient.builder()
                .user(patientUser1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());
        Patient patient2 = patientRepository.save(Patient.builder()
                .user(patientUser2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        // Create Admissions with Procedures and Prescriptions
        /*admissionRepository.saveAll(List.of(
                Admission.builder()
                        .admissionDateTime(LocalDateTime.now().minusDays(2))
                        .arrivedDate(LocalDateTime.now().minusDays(2).plusHours(1))
                        .patient(patient1)
                        .doctor(doctor1)
                        .administrator(admin)
                        .procedures(List.of(
                                Procedure.builder()
                                        .title("Teeth Cleaning")
                                        .description("Routine teeth cleaning procedure")
                                        .status(ProcedureStatus.COMPLETED)
                                        .price(0.0)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build(),
                                Procedure.builder()
                                        .title("Gastroscopy")
                                        .description("Stomach examination due to severe pain")
                                        .status(ProcedureStatus.COMPLETED)
                                        .price(0.0)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build()
                        ))
                        .prescriptions(List.of(
                                Prescription.builder()
                                        .title("Paracetamol")
                                        .description("Take 1 tablet 3 times a day for 5 days")
                                        .doctor(doctor1)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build()
                        ))
                        .description("Dental checkup and stomach pain")
                        .status(AdmissionStatus.COMPLETED)
                        .totalPrice(300000.0)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                Admission.builder()
                        .admissionDateTime(LocalDateTime.now().minusDays(1))
                        .arrivedDate(LocalDateTime.now().minusDays(1).plusHours(1))
                        .patient(patient1)
                        .doctor(doctor2)
                        .administrator(admin)
                        .procedures(List.of(
                                Procedure.builder()
                                        .title("Heart Checkup")
                                        .description("Comprehensive heart health checkup")
                                        .status(ProcedureStatus.COMPLETED)
                                        .price(0.0)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build()
                        ))
                        .prescriptions(List.of(
                                Prescription.builder()
                                        .title("Ibuprofen")
                                        .description("Take 1 tablet daily for 7 days")
                                        .doctor(doctor2)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build()
                        ))
                        .description("Heart examination")
                        .status(AdmissionStatus.COMPLETED)
                        .totalPrice(500000.0)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                Admission.builder()
                        .admissionDateTime(LocalDateTime.now().minusDays(1))
                        .arrivedDate(LocalDateTime.now().minusDays(1).plusHours(1))
                        .patient(patient2)
                        .doctor(doctor1)
                        .administrator(admin)
                        .procedures(List.of(
                                Procedure.builder()
                                        .title("Teeth Cleaning")
                                        .description("Routine teeth cleaning procedure")
                                        .status(ProcedureStatus.COMPLETED)
                                        .price(0.0)
                                        .createdAt(LocalDateTime.now())
                                        .updatedAt(LocalDateTime.now())
                                        .build()
                        ))
                        .prescriptions(List.of())
                        .description("Scheduled dental cleaning")
                        .status(AdmissionStatus.COMPLETED)
                        .totalPrice(0.0)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                Admission.builder()
                        .admissionDateTime(LocalDateTime.of(2025, 7, 18, 15, 0))
                        .patient(patient2)
                        .doctor(doctor1)
                        .administrator(admin)
                        .procedures(List.of())
                        .prescriptions(List.of())
                        .description("New dental appointment")
                        .status(AdmissionStatus.SCHEDULED)
                        .totalPrice(0.0)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        ));*/
    }
}