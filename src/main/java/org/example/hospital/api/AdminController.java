package org.example.hospital.api;

import lombok.RequiredArgsConstructor;
import org.example.hospital.dto.AdmissionReq;
import org.example.hospital.dto.UserReq;
import org.example.hospital.entity.Admission;
import org.example.hospital.entity.enums.AdmissionStatus;
import org.example.hospital.entity.users.*;
import org.example.hospital.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;
    private final AdmissionRepository admissionRepository;
    private final PatientRepository patientRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdministratorRepository administratorRepository;

    @GetMapping("/page")
    public Map<String, Object> adminPage() {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Patient> patients = patientRepository.findAll().stream()
                .filter(patient -> patient.getUser().getRoles().stream()
                        .anyMatch(role -> "ROLE_PATIENT".equals(role.getName())))
                .collect(Collectors.toList());
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctors);
        response.put("patients", patients);
        response.put("message", "Admin page data retrieved successfully.");
        return response;
    }

    @GetMapping("/patient/{id}")
    public Map<String, Object> getPatientHistory(@PathVariable UUID id) {
        Patient patient = patientRepository.findById(id)
                .filter(p -> p.getUser().getRoles().stream()
                        .anyMatch(role -> "ROLE_PATIENT".equals(role.getName())))
                .orElseThrow(() -> new IllegalArgumentException("Patient not found or not a valid patient"));
        List<Admission> admissions = admissionRepository.findAdmissionByPatientId(id);

        LocalDateTime now = LocalDateTime.now();
        for (Admission admission : admissions) {
            if (admission.getStatus() == AdmissionStatus.SCHEDULED &&
                    admission.getAdmissionDateTime().isBefore(now.minusMinutes(15))) {
                admission.setStatus(AdmissionStatus.LATE);
                admissionRepository.save(admission);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("selectedPatient", patient);
        response.put("admissions", admissions);
        response.put("message", "Patient history retrieved successfully.");
        return response;
    }

    @PostMapping("/addUser")
    public ResponseEntity<Map<String, Object>> addUser(@RequestBody UserReq userReq) {
        Role role = roleRepository.findByName("ROLE_PATIENT")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_PATIENT")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()));

        User user = User.builder()
                .firstName(userReq.firstName())
                .lastName(userReq.lastName())
                .phone(userReq.phone())
                .password(passwordEncoder.encode(userReq.password()))
                .birthDate(userReq.birthDate())
                .address(userReq.address())
                .roles(List.of(role))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        Patient patient = Patient.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        patientRepository.save(patient);

        Map<String, Object> response = new HashMap<>();
        response.put("patient", patient);
        response.put("message", "User added successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/schedule")
    public ResponseEntity<Map<String, Object>> schedule(@RequestBody AdmissionReq admissionReq,
                                                        @RequestParam UUID patientId,
                                                        @RequestParam UUID doctorId,
                                                        Authentication authentication) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
        User adminUser = userRepository.findUserByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Administrator not found"));
        Administrator admin = administratorRepository.findAll().stream()
                .filter(a -> a.getUser().getId().equals(adminUser.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Administrator not found"));

        LocalDateTime dateTime = LocalDateTime.parse(admissionReq.admissionDateTime());
        LocalDate requestedDate = dateTime.toLocalDate();

        List<Admission> sameDayAdmissions = admissionRepository.findAdmissionByPatientId(patientId)
                .stream()
                .filter(adm -> adm.getAdmissionDateTime().toLocalDate().equals(requestedDate))
                .filter(adm -> adm.getStatus() == AdmissionStatus.SCHEDULED ||
                        adm.getStatus() == AdmissionStatus.LATE ||
                        adm.getStatus() == AdmissionStatus.ARRIVED)
                .toList();
        if (!sameDayAdmissions.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Patient already has an active appointment today. Please complete or cancel it before scheduling another.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        List<Admission> existingAdmissions = admissionRepository.findByDoctorIdAndAdmissionDateTimeBetweenAndCancelledFalse(
                doctorId,
                dateTime.minusMinutes(15),
                dateTime.plusMinutes(15)
        );
        if (!existingAdmissions.isEmpty()) {
            String conflictingTimes = existingAdmissions.stream()
                    .map(adm -> adm.getAdmissionDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                    .collect(Collectors.joining(", "));
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Doctor is already scheduled at " + conflictingTimes + ". Please choose a different time.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Admission admission = Admission.builder()
                .description(admissionReq.description())
                .admissionDateTime(dateTime)
                .doctor(doctor)
                .patient(patient)
                .administrator(admin)
                .status(AdmissionStatus.SCHEDULED)
                .procedures(new ArrayList<>())
                .prescriptions(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        admissionRepository.save(admission);

        Map<String, Object> response = new HashMap<>();
        response.put("admission", admission);
        response.put("message", "Admission scheduled successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/acceptAdmission")
    public ResponseEntity<Map<String, Object>> acceptAdmission(@RequestParam UUID admissionId) {
        Admission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
        if (admission.getStatus() == AdmissionStatus.SCHEDULED || admission.getStatus() == AdmissionStatus.LATE) {
            admission.setStatus(AdmissionStatus.ARRIVED);
            admission.setArrivedDate(LocalDateTime.now());
            admissionRepository.save(admission);
            Map<String, Object> response = new HashMap<>();
            response.put("admission", admission);
            response.put("message", "Admission accepted successfully.");
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Admission cannot be accepted in its current state.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/cancelAdmission")
    public ResponseEntity<Map<String, Object>> cancelAdmission(@RequestParam UUID admissionId, @RequestParam String reason) {
        Admission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
        admission.cancel(reason);
        admissionRepository.save(admission);
        Map<String, Object> response = new HashMap<>();
        response.put("admission", admission);
        response.put("message", "Admission cancelled successfully.");
        return ResponseEntity.ok(response);
    }
}