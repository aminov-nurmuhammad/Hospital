package org.example.hospital.controller;

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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
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

    private void addSidebarAdmissions(Model model, Doctor doctor, LocalDate today) {
        List<Admission> activeAdmissions = admissionService.getOrderedAdmissionsForDoctor(doctor, today).stream()
                .filter(adm -> adm.getStatus() == AdmissionStatus.ARRIVED || adm.getStatus() == AdmissionStatus.IN_PROGRESS)
                .sorted(Comparator.comparing(Admission::getAdmissionDateTime))
                .collect(Collectors.toList());
        List<Admission> completedAdmissions = admissionService.getOrderedAdmissionsForDoctor(doctor, today).stream()
                .filter(adm -> adm.getStatus() == AdmissionStatus.COMPLETED)
                .sorted(Comparator.comparing(Admission::getAdmissionDateTime))
                .collect(Collectors.toList());
        model.addAttribute("activeAdmissions", activeAdmissions);
        model.addAttribute("completedAdmissions", completedAdmissions);
    }

    @GetMapping
    public String patients(@RequestParam(required = false) UUID patientId, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        User doctorUser = userRepository.findUserByPhone(username)
                .orElseThrow(() -> new IllegalArgumentException("Doctor user not found"));
        Doctor doctor = doctorRepository.findByUserId(doctorUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));

        LocalDate today = LocalDate.now(ASIA_TASHKENT);
        addSidebarAdmissions(model, doctor, today);
        model.addAttribute("doctorId", doctor.getId());

        if (patientId != null) {
            Patient patient = patientRepository.findById(patientId)
                    .filter(p -> p.getUser().getRoles().stream()
                            .anyMatch(role -> "ROLE_PATIENT".equals(role.getName())))
                    .orElseThrow(() -> new IllegalArgumentException("Patient not found or not a valid patient"));
            List<Admission> admissions = admissionRepository.findAdmissionByPatientId(patientId);
            model.addAttribute("patient", patient);
            model.addAttribute("admissions", admissions);

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
                model.addAttribute("admission", admission);
                model.addAttribute("procedures", admission.getProcedures());
                model.addAttribute("prescriptions", admission.getPrescriptions());
                model.addAttribute("procedurePriceSum", admission.getProcedures().stream()
                        .mapToDouble(Procedure::getPrice)
                        .sum());
                model.addAttribute("totalPrice", admission.getTotalPrice());
                model.addAttribute("readOnly", readOnly);
            } else {
                redirectAttributes.addFlashAttribute("error", "No active or completed admission found for this patient today.");
                return "redirect:/doctor";
            }
        } else {
            model.addAttribute("admissions", List.of());
        }

        return "doctor";
    }

    @GetMapping("/patient")
    public String patientProcedures(@RequestParam UUID patientId, Model model, Authentication authentication, RedirectAttributes redirectAttributes) {
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
            redirectAttributes.addFlashAttribute("error", "No active or completed admission found for this patient today.");
            return "redirect:/doctor";
        }

        boolean readOnly = admission.getStatus() == AdmissionStatus.COMPLETED;
        if (!readOnly && admission.getStatus() == AdmissionStatus.ARRIVED) {
            admission.setStatus(AdmissionStatus.IN_PROGRESS);
            admissionRepository.save(admission);
        }

        List<Procedure> procedures = admission.getProcedures();
        List<Prescription> prescriptions = admission.getPrescriptions();
        double procedurePriceSum = procedures.stream()
                .mapToDouble(Procedure::getPrice)
                .sum();
        List<Admission> admissions = admissionRepository.findAdmissionByPatientId(patientId);

        model.addAttribute("procedures", procedures);
        model.addAttribute("prescriptions", prescriptions);
        model.addAttribute("procedurePriceSum", procedurePriceSum);
        model.addAttribute("totalPrice", admission.getTotalPrice());
        model.addAttribute("readOnly", readOnly);
        model.addAttribute("patient", patient);
        model.addAttribute("admission", admission);
        model.addAttribute("admissions", admissions);
        model.addAttribute("doctorId", doctor.getId());

        addSidebarAdmissions(model, doctor, today);

        return "doctor";
    }

    @PostMapping("/patient/end")
    @Transactional
    public String finishPatient(@RequestParam UUID patientId, RedirectAttributes redirectAttributes) {
        Admission admission = admissionRepository.findTopByPatientIdAndStatusInOrderByAdmissionDateTimeDesc(
                patientId,
                List.of(AdmissionStatus.IN_PROGRESS)
        );
        if (admission == null) {
            redirectAttributes.addFlashAttribute("error", "No active admission found to complete for this patient.");
            return "redirect:/doctor";
        }

        admission.setStatus(AdmissionStatus.COMPLETED);
        for (Procedure procedure : admission.getProcedures()) {
            if (procedure.getStatus() == ProcedureStatus.SCHEDULED) {
                procedure.setStatus(ProcedureStatus.COMPLETED);
                procedureRepository.save(procedure);
            }
        }
        admissionRepository.save(admission);

        return "redirect:/doctor";
    }

    @PostMapping("/patient/addProcedure")
    @Transactional
    public String addProcedure(@RequestParam UUID patientId,
                               @RequestParam String title,
                               @RequestParam String description,
                               @RequestParam double price,
                               @RequestParam ProcedureStatus status,
                               RedirectAttributes redirectAttributes) {
        if (price < 0) {
            redirectAttributes.addFlashAttribute("error", "Procedure price cannot be negative.");
            return "redirect:/doctor/patient?patientId=" + patientId;
        }

        Admission admission = admissionRepository.findTopByPatientIdAndStatusInOrderByAdmissionDateTimeDesc(
                patientId,
                List.of(AdmissionStatus.IN_PROGRESS)
        );
        if (admission == null) {
            redirectAttributes.addFlashAttribute("error", "No active admission found for this patient.");
            return "redirect:/doctor";
        }

        Procedure procedure = Procedure.builder()
                .title(title)
                .description(description)
                .status(status)
                .price(price)
                .admission(admission)
                .build();

        admission.getProcedures().add(procedure);
        double procedurePriceSum = admission.getProcedures().stream()
                .mapToDouble(Procedure::getPrice)
                .sum();
        admission.setTotalPrice(procedurePriceSum);

        admissionRepository.save(admission);

        return "redirect:/doctor/patient?patientId=" + patientId;
    }

    @PostMapping("/patient/addPrescription")
    @Transactional
    public String addPrescription(@RequestParam UUID patientId,
                                  @RequestParam String title,
                                  @RequestParam String description,
                                  Authentication authentication,
                                  RedirectAttributes redirectAttributes) {
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
            redirectAttributes.addFlashAttribute("error", "No active admission found for this patient.");
            return "redirect:/doctor";
        }

        Prescription prescription = Prescription.builder()
                .title(title)
                .description(description)
                .admission(admission)
                .doctor(doctor)
                .build();

        admission.getPrescriptions().add(prescription);
        admissionRepository.save(admission);

        return "redirect:/doctor/patient?patientId=" + patientId;
    }

    @PostMapping("/patient/setTotalPrice")
    public String setTotalPrice(@RequestParam UUID patientId, @RequestParam double doctorPrice, RedirectAttributes redirectAttributes) {
        if (doctorPrice < 0) {
            redirectAttributes.addFlashAttribute("error", "Doctor's fee cannot be negative.");
            return "redirect:/doctor/patient?patientId=" + patientId;
        }

        Admission admission = admissionRepository.findTopByPatientIdAndStatusInOrderByAdmissionDateTimeDesc(
                patientId,
                List.of(AdmissionStatus.IN_PROGRESS)
        );
        if (admission == null) {
            redirectAttributes.addFlashAttribute("error", "No active admission found for this patient.");
            return "redirect:/doctor";
        }

        double procedurePriceSum = admission.getProcedures().stream()
                .mapToDouble(Procedure::getPrice)
                .sum();
        admission.setTotalPrice(procedurePriceSum + doctorPrice);
        admissionRepository.save(admission);
        return "redirect:/doctor/patient?patientId=" + patientId;
    }

    @PostMapping("/patient/updateProcedureStatus")
    @Transactional
    public String updateProcedureStatus(@RequestParam UUID procedureId,
                                        @RequestParam ProcedureStatus status,
                                        @RequestParam UUID patientId,
                                        RedirectAttributes redirectAttributes) {
        Procedure procedure = procedureRepository.findById(procedureId)
                .orElseThrow(() -> new IllegalArgumentException("Procedure not found"));
        procedure.setStatus(status);
        procedureRepository.save(procedure);
        return "redirect:/doctor/patient?patientId=" + patientId;
    }

    @PostMapping("/patient/updateDescription")
    @Transactional
    public String updateDescription(@RequestParam UUID patientId,
                                    @RequestParam String description,
                                    RedirectAttributes redirectAttributes) {
        if (description.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Description cannot be empty.");
            return "redirect:/doctor/patient?patientId=" + patientId;
        }

        Admission admission = admissionRepository.findTopByPatientIdAndStatusInOrderByAdmissionDateTimeDesc(
                patientId,
                List.of(AdmissionStatus.IN_PROGRESS)
        );
        if (admission == null) {
            redirectAttributes.addFlashAttribute("error", "No active admission found for this patient.");
            return "redirect:/doctor";
        }

        admission.setDescription(description);
        admissionRepository.save(admission);
        return "redirect:/doctor/patient?patientId=" + patientId;
    }
}