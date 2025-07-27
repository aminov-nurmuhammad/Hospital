package org.example.hospital.api;

import lombok.RequiredArgsConstructor;
import org.example.hospital.entity.Admission;
import org.example.hospital.entity.users.Patient;
import org.example.hospital.entity.users.User;
import org.example.hospital.repo.AdmissionRepository;
import org.example.hospital.repo.PatientRepository;
import org.example.hospital.repo.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("@RequestMapping(\"/api/admin\")\n/patient")
public class PatientApiController {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AdmissionRepository admissionRepository;

    @GetMapping
    public Map<String, Object> patientPage(Authentication authentication) {
        User user = userRepository.findUserByPhone(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Patient patient = patientRepository.findPatientByUserId(user.getId());

        List<Admission> admissions = admissionRepository.findAdmissionByPatientId(patient.getId());

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

        Map<String, Object> response = new HashMap<>();
        response.put("currentPatient", patient);
        response.put("admissions", admissions);
        response.put("user", user);
        response.put("waitStatuses", waitStatuses);
        response.put("message", "Patient data retrieved successfully.");
        return response;
    }
}