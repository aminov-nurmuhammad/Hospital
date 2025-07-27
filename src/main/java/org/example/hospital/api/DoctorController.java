package org.example.hospital.api;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.hospital.entity.Admission;
import org.example.hospital.entity.Prescription;
import org.example.hospital.entity.Procedure;
import org.example.hospital.entity.enums.AdmissionStatus;
import org.example.hospital.entity.enums.ProcedureStatus;
import org.example.hospital.entity.users.Doctor;
import org.example.hospital.entity.users.Patient;
import org.example.hospital.entity.users.User;
import org.example.hospital.repo.*;
import org.example.hospital.service.AdmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AdmissionRepository admissionRepository;
    private final ProcedureRepository procedureRepository;
    private final AdmissionService admissionService;

    private static final ZoneId ASIA_TASHKENT = ZoneId.of("Asia/Tashkent");

    @GetMapping
    public Map<String, Object> patients(@RequestParam(required = false) UUID patientId, Authentication authentication) {
        String username = authentication.getName();
        User doctorUser = userRepository.findUserByPhone(username)
                .orElseThrow(() -> new IllegalArgumentException("Doctor user not found"));
        Doctor doctor = doctorRepository.findByUserId(doctorUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        LocalDate today = LocalDate.now(ASIA_TASHKENT);
        List<Admission> activeAdmissions = admissionService.getOrderedAdmissionsForDoctor(doctor, today).stream()
                .filter(adm -> adm.getStatus() == AdmissionStatus.ARRIVED || adm.getStatus() == AdmissionStatus.IN_PROGRESS)
                .sorted(Comparator.comparing(Admission::getAdmissionDateTime))
                .collect(Collectors.toList());
        List<Admission> completedAdmissions = admissionService.getOrderedAdmissionsForDoctor(doctor, today).stream()
                .filter(adm -> adm.getStatus() == AdmissionStatus.COMPLETED)
                .sorted(Comparator.comparing(Admission::getAdmissionDateTime))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("activeAdmissions", activeAdmissions);
        response.put("completedAdmissions", completedAdmissions);
        response.put("doctorId", doctor.getId());
        response.put("message", "Doctor dashboard retrieved successfully.");

        if (patientId != null) {
            Patient patient = patientRepository.findById(patientId)
                    .filter(p -> p.getUser().getRoles().stream()
                            .anyMatch(role -> "ROLE_PATIENT".equals(role.getName())))
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found or not a valid patient"));
            List<Admission> admissions = admissionRepository.findAdmissionByPatientId(patientId);
            Admission admission = admissionRepository.findTopByPatientIdAndStatusInAndAdmissionDateTimeBetweenOrderByAdmissionDateTimeDesc(
                    patientId,
                    List.of(AdmissionStatus.ARRIVED, AdmissionStatus.IN_PROGRESS, AdmissionStatus.COMPLETED),
                    today.atStartOfDay(),
                    today.atTime(23, 59, 59)
            );
            if (admission != null) {
                boolean readOnly = admission.getStatus() == AdmissionStatus.COMPLETED;
                if (!readOnly && admission.getStatus() == AdmissionStatus.ARRIVED) {
                    admission.setStatus(AdmissionStatus.IN_PROGRESS);
                    admissionRepository.save(admission);
                }
                response.put("patient", patient);
                response.put("admissions", admissions);
                response.put("admission", admission);
                response.put("procedures", admission.getProcedures());
                response.put("prescriptions", admission.getPrescriptions());
                response.put("procedurePriceSum", admission.getProcedures().stream().mapToDouble(Procedure::getPrice).sum());
                response.put("totalPrice", admission.getTotalPrice());
                response.put("readOnly", readOnly);
            } else {
                response.put("message", "No active or completed admission found for this patient today.");
            }
        }
        return response;
    }

    @GetMapping("/patient")
    public Map<String, Object> patientProcedures(@RequestParam UUID patientId, Authentication authentication) {
        String username = authentication.getName();
        User doctorUser = userRepository.findUserByPhone(username)
                .orElseThrow(() -> new IllegalArgumentException("Doctor user not found"));
        Doctor doctor = doctorRepository.findByUserId(doctorUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        Patient patient = patientRepository.findById(patientId)
                .filter(p -> p.getUser().getRoles().stream()
                        .anyMatch(role -> "ROLE_PATIENT".equals(role.getName())))
                .orElseThrow(() -> new IllegalArgumentException("Patient not found or not a valid patient"));

        LocalDate today = LocalDate.now(ASIA_TASHKENT);
        Admission admission = admissionRepository.findTopByPatientIdAndStatusInAndAdmissionDateTimeBetweenOrderByAdmissionDateTimeDesc(
                patientId,
                List.of(AdmissionStatus.ARRIVED, AdmissionStatus.IN_PROGRESS, AdmissionStatus.COMPLETED),
                today.atStartOfDay(),
                today.atTime(23, 59, 59)
        );

        if (admission == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "No active or completed admission found for this patient today.");
            return response;
        }

        boolean readOnly = admission.getStatus() == AdmissionStatus.COMPLETED;
        if (!readOnly && admission.getStatus() == AdmissionStatus.ARRIVED) {
            admission.setStatus(AdmissionStatus.IN_PROGRESS);
            admissionRepository.save(admission);
        }

        List<Admission> admissions = admissionRepository.findAdmissionByPatientId(patientId);
        List<Admission> activeAdmissions = admissionService.getOrderedAdmissionsForDoctor(doctor, today).stream()
                .filter(adm -> adm.getStatus() == AdmissionStatus.ARRIVED || adm.getStatus() == AdmissionStatus.IN_PROGRESS)
                .sorted(Comparator.comparing(Admission::getAdmissionDateTime))
                .collect(Collectors.toList());
        List<Admission> completedAdmissions = admissionService.getOrderedAdmissionsForDoctor(doctor, today).stream()
                .filter(adm -> adm.getStatus() == AdmissionStatus.COMPLETED)
                .sorted(Comparator.comparing(Admission::getAdmissionDateTime))
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("patient", patient);
        response.put("admission", admission);
        response.put("admissions", admissions);
        response.put("procedures", admission.getProcedures());
        response.put("prescriptions", admission.getPrescriptions());
        response.put("procedurePriceSum", admission.getProcedures().stream().mapToDouble(Procedure::getPrice).sum());
        response.put("totalPrice", admission.getTotalPrice());
        response.put("readOnly", readOnly);
        response.put("doctorId", doctor.getId());
        response.put("activeAdmissions", activeAdmissions);
        response.put("completedAdmissions", completedAdmissions);
        response.put("message", "Patient procedures retrieved successfully.");
        return response;
    }

    @PostMapping("/patient/end")
    @Transactional
    public ResponseEntity<Map<String, Object>> finishPatient(@RequestParam UUID patientId) {
        Admission admission = admissionRepository.findTopByPatientIdAndStatusInOrderByAdmissionDateTimeDesc(
                patientId,
                List.of(AdmissionStatus.IN_PROGRESS)
        );
        if (admission == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "No active admission found to complete for this patient.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        admission.setStatus(AdmissionStatus.COMPLETED);
        for (Procedure procedure : admission.getProcedures()) {
            if (procedure.getStatus() == ProcedureStatus.SCHEDULED) {
                procedure.setStatus(ProcedureStatus.COMPLETED);
                procedureRepository.save(procedure);
            }
        }
        admissionRepository.save(admission);

        Map<String, Object> response = new HashMap<>();
        response.put("admission", admission);
        response.put("message", "Admission completed successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/patient/addProcedure")
    @Transactional
    public ResponseEntity<Map<String, Object>> addProcedure(@RequestParam UUID patientId,
                                                            @RequestParam String title,
                                                            @RequestParam String description,
                                                            @RequestParam double price,
                                                            @RequestParam ProcedureStatus status) {
        if (price < 0) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Procedure price cannot be negative.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Admission admission = admissionRepository.findTopByPatientIdAndStatusInOrderByAdmissionDateTimeDesc(
                patientId,
                List.of(AdmissionStatus.IN_PROGRESS)
        );
        if (admission == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "No active admission found for this patient.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Procedure procedure = Procedure.builder()
                .title(title)
                .description(description)
                .status(status)
                .price(price)
                .admission(admission)
                .build();

        admission.getProcedures().add(procedure);
        double procedurePriceSum = admission.getProcedures().stream().mapToDouble(Procedure::getPrice).sum();
        admission.setTotalPrice(procedurePriceSum);
        admissionRepository.save(admission);

        Map<String, Object> response = new HashMap<>();
        response.put("procedure", procedure);
        response.put("admission", admission);
        response.put("message", "Procedure added successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/patient/addPrescription")
    @Transactional
    public ResponseEntity<Map<String, Object>> addPrescription(@RequestParam UUID patientId,
                                                               @RequestParam String title,
                                                               @RequestParam String description,
                                                               Authentication authentication) {
        String username = authentication.getName();
        User doctorUser = userRepository.findUserByPhone(username)
                .orElseThrow(() -> new IllegalArgumentException("Doctor user not found"));
        Doctor doctor = doctorRepository.findByUserId(doctorUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        Admission admission = admissionRepository.findTopByPatientIdAndStatusInOrderByAdmissionDateTimeDesc(
                patientId,
                List.of(AdmissionStatus.IN_PROGRESS)
        );
        if (admission == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "No active admission found for this patient.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Prescription prescription = Prescription.builder()
                .title(title)
                .description(description)
                .admission(admission)
                .doctor(doctor)
                .build();

        admission.getPrescriptions().add(prescription);
        admissionRepository.save(admission);

        Map<String, Object> response = new HashMap<>();
        response.put("prescription", prescription);
        response.put("admission", admission);
        response.put("message", "Prescription added successfully.");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/patient/setTotalPrice")
    public ResponseEntity<Map<String, Object>> setTotalPrice(@RequestParam UUID patientId, @RequestParam double doctorPrice) {
        if (doctorPrice < 0) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Doctor's fee cannot be negative.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Admission admission = admissionRepository.findTopByPatientIdAndStatusInOrderByAdmissionDateTimeDesc(
                patientId,
                List.of(AdmissionStatus.IN_PROGRESS)
        );
        if (admission == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "No active admission found for this patient.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        double procedurePriceSum = admission.getProcedures().stream().mapToDouble(Procedure::getPrice).sum();
        admission.setTotalPrice(procedurePriceSum + doctorPrice);
        admissionRepository.save(admission);

        Map<String, Object> response = new HashMap<>();
        response.put("admission", admission);
        response.put("message", "Total price updated successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/patient/updateProcedureStatus")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateProcedureStatus(@RequestParam UUID procedureId,
                                                                     @RequestParam ProcedureStatus status,
                                                                     @RequestParam UUID patientId) {
        Procedure procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new IllegalArgumentException("Procedure not found"));
        procedure.setStatus(status);
        procedureRepository.save(procedure);

        Map<String, Object> response = new HashMap<>();
        response.put("procedure", procedure);
        response.put("message", "Procedure status updated successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/patient/updateDescription")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateDescription(@RequestParam UUID patientId,
                                                                 @RequestParam String description) {
        if (description.trim().isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Description cannot be empty.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Admission admission = admissionRepository.findTopByPatientIdAndStatusInOrderByAdmissionDateTimeDesc(
                patientId,
                List.of(AdmissionStatus.IN_PROGRESS)
        );
        if (admission == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "No active admission found for this patient.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        admission.setDescription(description);
        admissionRepository.save(admission);

        Map<String, Object> response = new HashMap<>();
        response.put("admission", admission);
        response.put("message", "Description updated successfully.");
        return ResponseEntity.ok(response);
    }
}