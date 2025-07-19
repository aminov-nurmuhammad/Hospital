package org.example.hospital.controller;

import lombok.RequiredArgsConstructor;
import org.example.hospital.dto.AdmissionReq;
import org.example.hospital.dto.UserReq;
import org.example.hospital.entity.Admission;
import org.example.hospital.entity.enums.AdmissionStatus;
import org.example.hospital.entity.users.*;
import org.example.hospital.repo.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
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
    public String adminPage(Model model) {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Patient> patients = patientRepository.findAll().stream()
                .filter(patient -> patient.getUser().getRoles().stream()
                        .anyMatch(role -> "ROLE_PATIENT".equals(role.getName())))
                .collect(Collectors.toList());
        model.addAttribute("doctors", doctors);
        model.addAttribute("patients", patients);
        return "adminPage";
    }

    @GetMapping("/patient/{id}")
    public String getPatientHistory(@PathVariable UUID id, Model model) {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Patient> patients = patientRepository.findAll().stream()
                .filter(patient -> patient.getUser().getRoles().stream()
                        .anyMatch(role -> "ROLE_PATIENT".equals(role.getName())))
                .collect(Collectors.toList());
        Patient patient = patientRepository.findById(id)
                .filter(p -> p.getUser().getRoles().stream()
                        .anyMatch(role -> "ROLE_PATIENT".equals(role.getName())))
                .orElseThrow(() -> new IllegalArgumentException("Patient not found or not a valid patient"));
        List<Admission> admissions = admissionRepository.findAdmissionByPatientId(id);

        // Check for late admissions
        LocalDateTime now = LocalDateTime.now();
        for (Admission admission : admissions) {
            if (admission.getStatus() == AdmissionStatus.SCHEDULED &&
                    admission.getAdmissionDateTime().isBefore(now.minusMinutes(15))) {
                admission.setStatus(AdmissionStatus.LATE);
                admissionRepository.save(admission);
            }
        }

        model.addAttribute("doctors", doctors);
        model.addAttribute("patients", patients);
        model.addAttribute("selectedPatient", patient);
        model.addAttribute("admissions", admissions);
        return "adminPage";
    }

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute UserReq userReq) {
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

        return "redirect:/admin/page";
    }

    @PostMapping("/schedule")
    public String schedule(@ModelAttribute AdmissionReq admissionReq,
                           @RequestParam UUID patientId,
                           @RequestParam UUID doctorId,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
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

        // Check for active appointments (SCHEDULED, LATE, ARRIVED) on the same day
        List<Admission> sameDayAdmissions = admissionRepository.findAdmissionByPatientId(patientId)
                .stream()
                .filter(adm -> adm.getAdmissionDateTime().toLocalDate().equals(requestedDate))
                .filter(adm -> adm.getStatus() == AdmissionStatus.SCHEDULED ||
                        adm.getStatus() == AdmissionStatus.LATE ||
                        adm.getStatus() == AdmissionStatus.ARRIVED)
                .toList();
        if (!sameDayAdmissions.isEmpty()) {
            redirectAttributes.addFlashAttribute("sameDayError",
                    "Patient already has an active appointment today. Please complete or cancel it before scheduling another.");
            return "redirect:/admin/patient/" + patientId;
        }

        // Check for overlapping appointments (within 15 minutes)
        List<Admission> existingAdmissions = admissionRepository.findByDoctorIdAndAdmissionDateTimeBetweenAndCancelledFalse(
                doctorId,
                dateTime.minusMinutes(15),
                dateTime.plusMinutes(15)
        );
        if (!existingAdmissions.isEmpty()) {
            String conflictingTimes = existingAdmissions.stream()
                    .map(adm -> adm.getAdmissionDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")))
                    .collect(Collectors.joining(", "));
            redirectAttributes.addFlashAttribute("scheduleError",
                    "Doctor is already scheduled at " + conflictingTimes + ". Please choose a different time.");
            return "redirect:/admin/patient/" + patientId;
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
        return "redirect:/admin/patient/" + patientId;
    }

    @PostMapping("/acceptAdmission")
    public String acceptAdmission(@RequestParam UUID admissionId) {
        Admission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
        if (admission.getStatus() == AdmissionStatus.SCHEDULED || admission.getStatus() == AdmissionStatus.LATE) {
            admission.setStatus(AdmissionStatus.ARRIVED);
            admission.setArrivedDate(LocalDateTime.now());
            admissionRepository.save(admission);
        }
        return "redirect:/admin/patient/" + admission.getPatient().getId();
    }

    @PostMapping("/cancelAdmission")
    public String cancelAdmission(@RequestParam UUID admissionId, @RequestParam String reason) {
        Admission admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new IllegalArgumentException("Admission not found"));
        admission.cancel(reason);
        admissionRepository.save(admission);
        return "redirect:/admin/patient/" + admission.getPatient().getId();
    }
}