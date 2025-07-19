package org.example.hospital.controller;

import lombok.RequiredArgsConstructor;
import org.example.hospital.entity.Admission;
import org.example.hospital.entity.users.Patient;
import org.example.hospital.entity.users.User;
import org.example.hospital.repo.AdmissionRepository;
import org.example.hospital.repo.PatientRepository;
import org.example.hospital.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/patient")
public class PatientController {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AdmissionRepository admissionRepository;

    @GetMapping
    public String patientPage(Model model, Authentication authentication) {
        User user = userRepository.findUserByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Patient patient = patientRepository.findPatientByUserId(user.getId());

        List<Admission> admissions = admissionRepository.findAdmissionByPatientId(patient.getId());

        // Compute waitStatus for each admission
        Map<UUID, String> waitStatuses = new HashMap<>();
        for (Admission admission : admissions) {
            String waitStatus;
            if (admission.getArrivedDate() == null) {
                waitStatus = "Scheduled";
            } else {
                Duration duration = Duration.between(admission.getAdmissionDateTime(), admission.getArrivedDate());
                waitStatus = duration.toMinutes() <= 15 ? "Arrived" : "Late";
            }
            waitStatuses.put(admission.getId(), waitStatus);
        }

        model.addAttribute("currentPatient", patient);
        model.addAttribute("admissions", admissions);
        model.addAttribute("user", user);
        model.addAttribute("waitStatuses", waitStatuses);
        return "userPage";
    }
}