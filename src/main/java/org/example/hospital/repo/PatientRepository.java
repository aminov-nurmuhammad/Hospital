package org.example.hospital.repo;

import org.example.hospital.entity.users.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {
    Patient findPatientByUserId(UUID userId);
}