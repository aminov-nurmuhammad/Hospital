package org.example.hospital.api;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.hospital.dto.DoctorReq;
import org.example.hospital.dto.PatientReq;
import org.example.hospital.entity.Admission;
import org.example.hospital.entity.Speciality;
import org.example.hospital.entity.users.Doctor;
import org.example.hospital.entity.users.Patient;
import org.example.hospital.entity.users.Role;
import org.example.hospital.entity.users.User;
import org.example.hospital.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@RestController
@RequiredArgsConstructor
@RequestMapping("/super")
public class SuperAdminController {

    private final DoctorRepository doctorRepository;
    private final SpecialityRepository specialityRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final AdmissionRepository admissionRepository;

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+998[0-9]{9}$");

    @GetMapping("/doctors")
    public Map<String, Object> getDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Speciality> specialities = specialityRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctors);
        response.put("specialities", specialities);
        response.put("message", "Doctors and specialities retrieved successfully.");
        return response;
    }

    @GetMapping("/specialities")
    public Map<String, Object> getSpeciality() {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Speciality> specialities = specialityRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("doctors", doctors);
        response.put("specialities", specialities);
        response.put("message", "Specialities retrieved successfully.");
        return response;
    }

    @PostMapping("/doctors/add")
    @Transactional
    public ResponseEntity<Map<String, Object>> addDoctor(@RequestBody DoctorReq doctorReq, @RequestParam UUID speciality) {
        if (doctorReq.firstName() == null || doctorReq.firstName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "First name is required."));
        }
        if (doctorReq.lastName() == null || doctorReq.lastName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Last name is required."));
        }
        if (doctorReq.phone() == null || !PHONE_PATTERN.matcher(doctorReq.phone()).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid phone number. Must be in format +998xxxxxxxxx."));
        }
        if (doctorReq.password() == null || doctorReq.password().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Password must be at least 6 characters."));
        }
        if (doctorReq.address() == null || doctorReq.address().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Address is required."));
        }
        if (doctorReq.birthDate() == null || doctorReq.birthDate().isAfter(LocalDate.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Birth date is required and must not be in the future."));
        }
        if (userRepository.findUserByPhone(doctorReq.phone()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Phone number already exists."));
        }

        Speciality speciality1 = specialityRepository.findById(speciality)
                .orElseThrow(() -> new IllegalArgumentException("Speciality not found"));

        Role role = roleRepository.findByName("ROLE_DOCTOR")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_DOCTOR")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()));

        User user = User.builder()
                .firstName(doctorReq.firstName())
                .lastName(doctorReq.lastName())
                .phone(doctorReq.phone())
                .password(passwordEncoder.encode(doctorReq.password()))
                .address(doctorReq.address())
                .birthDate(doctorReq.birthDate())
                .roles(List.of(role))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .speciality(speciality1)
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        doctorRepository.save(doctor);

        Map<String, Object> response = new HashMap<>();
        response.put("doctor", doctor);
        response.put("message", "Doctor added successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/speciality/add")
    @Transactional
    public ResponseEntity<Map<String, Object>> addSpec(@RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Speciality name is required."));
        }
        if (specialityRepository.findByName(name).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Speciality name already exists."));
        }

        Speciality speciality = Speciality.builder()
                .name(name)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        specialityRepository.save(speciality);

        Map<String, Object> response = new HashMap<>();
        response.put("speciality", speciality);
        response.put("message", "Speciality added successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/speciality/edit")
    @Transactional
    public ResponseEntity<Map<String, Object>> editSpec(@RequestParam UUID id, @RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Speciality name is required."));
        }
        Speciality existingSpeciality = specialityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Speciality not found"));
        if (specialityRepository.findByName(name).isPresent() && !existingSpeciality.getName().equals(name)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Speciality name already exists."));
        }

        existingSpeciality.setName(name);
        existingSpeciality.setUpdatedAt(LocalDateTime.now());
        specialityRepository.save(existingSpeciality);

        Map<String, Object> response = new HashMap<>();
        response.put("speciality", existingSpeciality);
        response.put("message", "Speciality updated successfully.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/speciality/delete")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteSpec(@RequestParam UUID id) {
        if (!specialityRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Speciality not found."));
        }
        specialityRepository.deleteById(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Speciality deleted successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/doctors/{id}")
    public Doctor getDoctor(@PathVariable UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
    }

    @PutMapping("/doctors/edit")
    @Transactional
    public ResponseEntity<Map<String, Object>> editDoctor(@RequestBody DoctorReq doctorReq, @RequestParam UUID speciality, @RequestParam UUID id) {
        if (doctorReq.firstName() == null || doctorReq.firstName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "First name is required."));
        }
        if (doctorReq.lastName() == null || doctorReq.lastName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Last name is required."));
        }
        if (doctorReq.phone() == null || !PHONE_PATTERN.matcher(doctorReq.phone()).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid phone number. Must be in format +998xxxxxxxxx."));
        }
        if (doctorReq.address() == null || doctorReq.address().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Address is required."));
        }
        if (doctorReq.birthDate() == null || doctorReq.birthDate().isAfter(LocalDate.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Birth date is required and must not be in the future."));
        }

        Optional<Doctor> optionalDoctor = doctorRepository.findById(id);
        if (optionalDoctor.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found."));
        }

        Optional<User> userWithPhone = userRepository.findUserByPhone(doctorReq.phone());
        if (userWithPhone.isPresent() && !userWithPhone.get().getId().equals(optionalDoctor.get().getUser().getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Phone number already exists."));
        }

        Doctor doctor = optionalDoctor.get();
        Speciality speciality1 = specialityRepository.findById(speciality)
                .orElseThrow(() -> new IllegalArgumentException("Speciality not found"));

        User user = doctor.getUser();
        user.setFirstName(doctorReq.firstName());
        user.setLastName(doctorReq.lastName());
        user.setPhone(doctorReq.phone());
        user.setAddress(doctorReq.address());
        user.setBirthDate(doctorReq.birthDate());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        doctor.setSpeciality(speciality1);
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(doctor);

        Map<String, Object> response = new HashMap<>();
        response.put("doctor", doctor);
        response.put("message", "Doctor updated successfully.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/doctors/delete")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteDoctor(@RequestParam UUID id) {
        Optional<Doctor> optionalDoctor = doctorRepository.findById(id);
        if (optionalDoctor.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Doctor not found."));
        }
        Doctor doctor = optionalDoctor.get();
        User user = doctor.getUser();
        doctorRepository.deleteById(id);
        userRepository.delete(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Doctor and associated user deleted successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patients")
    public Map<String, Object> getPatients() {
        List<Patient> patients = patientRepository.findAll();
        Map<String, Object> response = new HashMap<>();
        response.put("patients", patients);
        response.put("message", "Patients retrieved successfully.");
        return response;
    }

    @PostMapping("/patients/add")
    @Transactional
    public ResponseEntity<Map<String, Object>> addPatient(@RequestBody PatientReq patientReq) {
        if (patientReq.getFirstName() == null || patientReq.getFirstName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "First name is required."));
        }
        if (patientReq.getLastName() == null || patientReq.getLastName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Last name is required."));
        }
        if (patientReq.getPhone() == null || !PHONE_PATTERN.matcher(patientReq.getPhone()).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid phone number. Must be in format +998xxxxxxxxx."));
        }
        if (patientReq.getPassword() == null || patientReq.getPassword().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Password must be at least 6 characters."));
        }
        if (patientReq.getAddress() == null || patientReq.getAddress().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Address is required."));
        }
        if (patientReq.getBirthDate() == null || patientReq.getBirthDate().isAfter(LocalDate.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Birth date is required and must not be in the future."));
        }
        if (userRepository.findUserByPhone(patientReq.getPhone()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Phone number already exists."));
        }

        Role role = roleRepository.findByName("ROLE_PATIENT")
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name("ROLE_PATIENT")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()));

        User user = User.builder()
                .firstName(patientReq.getFirstName())
                .lastName(patientReq.getLastName())
                .phone(patientReq.getPhone())
                .password(passwordEncoder.encode(patientReq.getPassword()))
                .address(patientReq.getAddress())
                .birthDate(patientReq.getBirthDate())
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
        response.put("message", "Patient added successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/patients/edit")
    @Transactional
    public ResponseEntity<Map<String, Object>> editPatient(@RequestBody PatientReq patientReq, @RequestParam UUID id) {
        if (patientReq.getFirstName() == null || patientReq.getFirstName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "First name is required."));
        }
        if (patientReq.getLastName() == null || patientReq.getLastName().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Last name is required."));
        }
        if (patientReq.getPhone() == null || !PHONE_PATTERN.matcher(patientReq.getPhone()).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid phone number. Must be in format +998xxxxxxxxx."));
        }
        if (patientReq.getAddress() == null || patientReq.getAddress().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Address is required."));
        }
        if (patientReq.getBirthDate() == null || patientReq.getBirthDate().isAfter(LocalDate.now())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Birth date is required and must not be in the future."));
        }

        Optional<Patient> optionalPatient = patientRepository.findById(id);
        if (optionalPatient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Patient not found."));
        }

        Optional<User> userWithPhone = userRepository.findUserByPhone(patientReq.getPhone());
        if (userWithPhone.isPresent() && !userWithPhone.get().getId().equals(optionalPatient.get().getUser().getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Phone number already exists."));
        }

        Patient patient = optionalPatient.get();
        User user = patient.getUser();

        user.setFirstName(patientReq.getFirstName());
        user.setLastName(patientReq.getLastName());
        user.setPhone(patientReq.getPhone());
        user.setAddress(patientReq.getAddress());
        user.setBirthDate(patientReq.getBirthDate());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        patient.setUpdatedAt(LocalDateTime.now());
        patientRepository.save(patient);

        Map<String, Object> response = new HashMap<>();
        response.put("patient", patient);
        response.put("message", "Patient updated successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/patients/{id}")
    public Patient getPatient(@PathVariable UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
    }

    @DeleteMapping("/patients/delete")
    @Transactional
    public ResponseEntity<Map<String, Object>> deletePatient(@RequestParam UUID id) {
        Optional<Patient> optionalPatient = patientRepository.findById(id);
        if (optionalPatient.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Patient not found."));
        }
        Patient patient = optionalPatient.get();
        UUID patientId = patient.getId();
        User user = patient.getUser();

        List<Admission> admissions = admissionRepository.findByPatientId(patientId);
        admissionRepository.deleteAll(admissions);
        patientRepository.delete(patient);
        userRepository.delete(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Patient and all related data deleted successfully.");
        return ResponseEntity.ok(response);
    }
}