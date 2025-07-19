package org.example.hospital.controller;

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
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Controller
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
    public String getDoctors(Model model, Authentication authentication) {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Speciality> specialities = specialityRepository.findAll();
        model.addAttribute("doctors", doctors);
        model.addAttribute("specialities", specialities);
        return "doctors";
    }

    @GetMapping("/specialities")
    public String getSpeciality(Model model, Authentication authentication) {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Speciality> specialities = specialityRepository.findAll();
        model.addAttribute("doctors", doctors);
        model.addAttribute("specialities", specialities);
        return "speciality";
    }

    @PostMapping("/doctors/add")
    @Transactional
    public String addDoctor(@ModelAttribute DoctorReq doctorReq, @RequestParam UUID speciality, RedirectAttributes redirectAttributes) {
        try {
            if (doctorReq.firstName() == null || doctorReq.firstName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "First name is required.");
                return "redirect:/super/doctors";
            }
            if (doctorReq.lastName() == null || doctorReq.lastName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Last name is required.");
                return "redirect:/super/doctors";
            }
            if (doctorReq.phone() == null || !PHONE_PATTERN.matcher(doctorReq.phone()).matches()) {
                redirectAttributes.addFlashAttribute("error", "Invalid phone number. Must be in format +998xxxxxxxxx.");
                return "redirect:/super/doctors";
            }
            if (doctorReq.password() == null || doctorReq.password().length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters.");
                return "redirect:/super/doctors";
            }
            if (doctorReq.address() == null || doctorReq.address().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Address is required.");
                return "redirect:/super/doctors";
            }
            if (doctorReq.birthDate() == null || doctorReq.birthDate().isAfter(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "Birth date is required and must not be in the future.");
                return "redirect:/super/doctors";
            }
            if (userRepository.findUserByPhone(doctorReq.phone()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Phone number already exists.");
                return "redirect:/super/doctors";
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
            redirectAttributes.addFlashAttribute("success", "Doctor added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add doctor: " + e.getMessage());
        }
        return "redirect:/super/doctors";
    }

    @PostMapping("/speciality/add")
    @Transactional
    public String addSpec(@RequestParam String name, RedirectAttributes redirectAttributes) {
        try {
            if (name == null || name.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Speciality name is required.");
                return "redirect:/super/specialities";
            }
            if (specialityRepository.findByName(name).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Speciality name already exists.");
                return "redirect:/super/specialities";
            }

            Speciality speciality = Speciality.builder()
                    .name(name)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            specialityRepository.save(speciality);
            redirectAttributes.addFlashAttribute("success", "Speciality added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add speciality: " + e.getMessage());
        }
        return "redirect:/super/specialities";
    }

    @PostMapping("/speciality/edit")
    @Transactional
    public String editSpec(@RequestParam UUID id, @RequestParam String name, RedirectAttributes redirectAttributes) {
        try {
            if (name == null || name.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Speciality name is required.");
                return "redirect:/super/specialities";
            }
            if (specialityRepository.findByName(name).isPresent() && !specialityRepository.findById(id).get().getName().equals(name)) {
                redirectAttributes.addFlashAttribute("error", "Speciality name already exists.");
                return "redirect:/super/specialities";
            }

            Speciality speciality = specialityRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Speciality not found"));
            speciality.setName(name);
            speciality.setUpdatedAt(LocalDateTime.now());
            specialityRepository.save(speciality);
            redirectAttributes.addFlashAttribute("success", "Speciality updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update speciality: " + e.getMessage());
        }
        return "redirect:/super/specialities";
    }

    @GetMapping("/speciality/delete")
    @Transactional
    public String deleteSpec(@RequestParam UUID id, RedirectAttributes redirectAttributes) {
        try {
            if (!specialityRepository.existsById(id)) {
                redirectAttributes.addFlashAttribute("error", "Speciality not found.");
                return "redirect:/super/specialities";
            }
            specialityRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Speciality deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete speciality: " + e.getMessage());
        }
        return "redirect:/super/specialities";
    }

    @GetMapping("/doctors/{id}")
    @ResponseBody
    public Doctor getDoctor(@PathVariable UUID id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
    }

    @PostMapping("/doctors/edit")
    @Transactional
    public String editDoctor(@ModelAttribute DoctorReq doctorReq, @RequestParam UUID speciality, @RequestParam UUID id, RedirectAttributes redirectAttributes) {
        try {
            if (doctorReq.firstName() == null || doctorReq.firstName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "First name is required.");
                return "redirect:/super/doctors";
            }
            if (doctorReq.lastName() == null || doctorReq.lastName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Last name is required.");
                return "redirect:/super/doctors";
            }
            if (doctorReq.phone() == null || !PHONE_PATTERN.matcher(doctorReq.phone()).matches()) {
                redirectAttributes.addFlashAttribute("error", "Invalid phone number. Must be in format +998xxxxxxxxx.");
                return "redirect:/super/doctors";
            }
            if (doctorReq.address() == null || doctorReq.address().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Address is required.");
                return "redirect:/super/doctors";
            }
            if (doctorReq.birthDate() == null || doctorReq.birthDate().isAfter(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "Birth date is required and must not be in the future.");
                return "redirect:/super/doctors";
            }

            Optional<Doctor> optionalDoctor = doctorRepository.findById(id);
            if (optionalDoctor.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found.");
                return "redirect:/super/doctors";
            }

            Optional<User> userWithPhone = userRepository.findUserByPhone(doctorReq.phone());
            if (userWithPhone.isPresent() && !userWithPhone.get().getId().equals(optionalDoctor.get().getUser().getId())) {
                redirectAttributes.addFlashAttribute("error", "Phone number already exists.");
                return "redirect:/super/doctors";
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
            redirectAttributes.addFlashAttribute("success", "Doctor updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update doctor: " + e.getMessage());
        }
        return "redirect:/super/doctors";
    }

    @GetMapping("/deleteDoctor")
    @Transactional
    public String deleteDoctor(@RequestParam UUID id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Doctor> optionalDoctor = doctorRepository.findById(id);
            if (optionalDoctor.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Doctor not found.");
                return "redirect:/super/doctors";
            }
            Doctor doctor = optionalDoctor.get();
            User user = doctor.getUser();
            doctorRepository.deleteById(id);
            userRepository.delete(user);
            redirectAttributes.addFlashAttribute("success", "Doctor and associated user deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete doctor: " + e.getMessage());
        }
        return "redirect:/super/doctors";
    }

    @GetMapping("/patients")
    public String getPatients(Model model, Authentication authentication) {
        List<Patient> patients = patientRepository.findAll();
        model.addAttribute("patients", patients);
        return "patients";
    }

    @PostMapping("/patients/add")
    @Transactional
    public String addPatient(@ModelAttribute PatientReq patientReq, RedirectAttributes redirectAttributes) {
        try {
            if (patientReq.getFirstName() == null || patientReq.getFirstName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "First name is required.");
                return "redirect:/super/patients";
            }
            if (patientReq.getLastName() == null || patientReq.getLastName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Last name is required.");
                return "redirect:/super/patients";
            }
            if (patientReq.getPhone() == null || !PHONE_PATTERN.matcher(patientReq.getPhone()).matches()) {
                redirectAttributes.addFlashAttribute("error", "Invalid phone number. Must be in format +998xxxxxxxxx.");
                return "redirect:/super/patients";
            }
            if (patientReq.getPassword() == null || patientReq.getPassword().length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 6 characters.");
                return "redirect:/super/patients";
            }
            if (patientReq.getAddress() == null || patientReq.getAddress().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Address is required.");
                return "redirect:/super/patients";
            }
            if (patientReq.getBirthDate() == null || patientReq.getBirthDate().isAfter(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "Birth date is required and must not be in the future.");
                return "redirect:/super/patients";
            }
            if (userRepository.findUserByPhone(patientReq.getPhone()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Phone number already exists.");
                return "redirect:/super/patients";
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
            redirectAttributes.addFlashAttribute("success", "Patient added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add patient: " + e.getMessage());
        }
        return "redirect:/super/patients";
    }

    @PostMapping("/patients/edit")
    @Transactional
    public String editPatient(@ModelAttribute PatientReq patientReq, @RequestParam UUID id, RedirectAttributes redirectAttributes) {
        try {
            if (patientReq.getFirstName() == null || patientReq.getFirstName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "First name is required.");
                return "redirect:/super/patients";
            }
            if (patientReq.getLastName() == null || patientReq.getLastName().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Last name is required.");
                return "redirect:/super/patients";
            }
            if (patientReq.getPhone() == null || !PHONE_PATTERN.matcher(patientReq.getPhone()).matches()) {
                redirectAttributes.addFlashAttribute("error", "Invalid phone number. Must be in format +998xxxxxxxxx.");
                return "redirect:/super/patients";
            }
            if (patientReq.getAddress() == null || patientReq.getAddress().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Address is required.");
                return "redirect:/super/patients";
            }
            if (patientReq.getBirthDate() == null || patientReq.getBirthDate().isAfter(LocalDate.now())) {
                redirectAttributes.addFlashAttribute("error", "Birth date is required and must not be in the future.");
                return "redirect:/super/patients";
            }

            Optional<Patient> optionalPatient = patientRepository.findById(id);
            if (optionalPatient.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Patient not found.");
                return "redirect:/super/patients";
            }

            Optional<User> userWithPhone = userRepository.findUserByPhone(patientReq.getPhone());
            if (userWithPhone.isPresent() && !userWithPhone.get().getId().equals(optionalPatient.get().getUser().getId())) {
                redirectAttributes.addFlashAttribute("error", "Phone number already exists.");
                return "redirect:/super/patients";
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
            redirectAttributes.addFlashAttribute("success", "Patient updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update patient: " + e.getMessage());
        }
        return "redirect:/super/patients";
    }

    @GetMapping("/patients/{id}")
    @ResponseBody
    public Patient getPatient(@PathVariable UUID id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));
    }

    @GetMapping("/deletePatient")
    @Transactional
    public String deletePatient(@RequestParam UUID id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Patient> optionalPatient = patientRepository.findById(id);
            if (optionalPatient.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Patient not found.");
                return "redirect:/super/patients";
            }
            Patient patient = optionalPatient.get();
            UUID patientId = patient.getId();
            User user = patient.getUser();

            List<Admission> admissions = admissionRepository.findByPatientId(patientId);
            admissionRepository.deleteAll(admissions);
            patientRepository.delete(patient);
            userRepository.delete(user);
            redirectAttributes.addFlashAttribute("success", "Patient and all related data deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete patient: " + e.getMessage());
        }
        return "redirect:/super/patients";
    }

}