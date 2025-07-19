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

        User adminUser = userRepository.save(User.builder()
                .firstName("Kamola")
                .lastName("Rustamova")
                .phone("+998901234001")
                .password(passwordEncoder.encode("admin"))
                .birthDate(LocalDate.of(1984, 4, 12))
                .address("23 Afrosiyob Street, Tashkent, Uzbekistan")
                .roles(List.of(adminRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        Administrator admin = administratorRepository.save(Administrator.builder()
                .user(adminUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        User superAdminUser = userRepository.save(User.builder()
                .firstName("Javohir")
                .lastName("Nasriddinov")
                .phone("+998901234002")
                .password(passwordEncoder.encode("superadmin"))
                .birthDate(LocalDate.of(1976, 6, 5))
                .address("78 Buyuk Ipak Yo‘li, Tashkent, Uzbekistan")
                .roles(List.of(superAdminRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        administratorRepository.save(Administrator.builder()
                .user(superAdminUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        User doctorUser1 = userRepository.save(User.builder()
                .firstName("Iskandar")
                .lastName("Khodjaev")
                .phone("+998901234003")
                .password(passwordEncoder.encode("doctor"))
                .birthDate(LocalDate.of(1982, 3, 14))
                .address("145 Shaykhontohur District, Tashkent")
                .roles(List.of(doctorRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        User doctorUser2 = userRepository.save(User.builder()
                .firstName("Dilorom")
                .lastName("Saidova")
                .phone("+998901234004")
                .password(passwordEncoder.encode("doctor2"))
                .birthDate(LocalDate.of(1979, 12, 22))
                .address("56 Yakkasaray District, Tashkent")
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

        User patientUser1 = userRepository.save(User.builder()
                .firstName("Sherzod")
                .lastName("Mahkamov")
                .phone("+998901234005")
                .password(passwordEncoder.encode("patient"))
                .birthDate(LocalDate.of(1992, 9, 17))
                .address("33 Olmazor District, Tashkent")
                .roles(List.of(patientRole))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        User patientUser2 = userRepository.save(User.builder()
                .firstName("Nigora")
                .lastName("Rashidova")
                .phone("+998901234006")
                .password(passwordEncoder.encode("patient2"))
                .birthDate(LocalDate.of(1996, 1, 8))
                .address("44 Chilanzar District, Tashkent")
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