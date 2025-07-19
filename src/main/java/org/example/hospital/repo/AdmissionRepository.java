package org.example.hospital.repo;

import org.example.hospital.entity.Admission;
import org.example.hospital.entity.enums.AdmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface AdmissionRepository extends JpaRepository<Admission, UUID> {
    List<Admission> findByDoctorIdOrderByArrivedDateAsc(UUID doctorId);

    List<Admission> findAllByPatientId(UUID patientId);

    Admission findTopByPatientIdOrderByAdmissionDateTimeDesc(UUID patientId);

    List<Admission> findAdmissionByPatientId(UUID id);

    List<Admission> findByDoctorIdAndAdmissionDateTimeBetweenAndCancelledFalseOrderByStatusAscAdmissionDateTimeAscArrivedDateAscCreatedAtAsc(
            UUID doctorId, LocalDateTime start, LocalDateTime end);

    List<Admission> findByDoctorIdAndAdmissionDateTimeBetweenAndCancelledFalse(UUID doctorId, LocalDateTime minus, LocalDateTime plus);

    Collection<Object> findByDoctorIdAndAdmissionDateTimeBetween(UUID id, LocalDateTime localDateTime, LocalDateTime localDateTime1);

    Admission findTopByPatientIdAndStatusInAndAdmissionDateTimeBetweenOrderByAdmissionDateTimeDesc(UUID patientId, List<AdmissionStatus> arrived, LocalDateTime localDateTime, LocalDateTime localDateTime1);

    Admission findTopByPatientIdAndStatusInOrderByAdmissionDateTimeDesc(UUID patientId, List<AdmissionStatus> inProgress);

    List<Admission> findByPatientId(UUID id);

    Collection<Object> findByDoctorId(UUID id);
}