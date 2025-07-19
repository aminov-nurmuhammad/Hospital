package org.example.hospital.service;

import org.example.hospital.entity.Admission;
import org.example.hospital.entity.users.Doctor;
import org.example.hospital.repo.AdmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AdmissionService {

    @Autowired
    private AdmissionRepository admissionRepository;

    public List<Admission> getOrderedAdmissionsForDoctor(Doctor doctor, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return admissionRepository.findByDoctorIdAndAdmissionDateTimeBetweenAndCancelledFalseOrderByStatusAscAdmissionDateTimeAscArrivedDateAscCreatedAtAsc(
                doctor.getId(), startOfDay, endOfDay);
    }
}