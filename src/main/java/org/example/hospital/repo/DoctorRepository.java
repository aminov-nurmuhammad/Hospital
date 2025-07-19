package org.example.hospital.repo;

import org.example.hospital.entity.users.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    Optional<Doctor> findByUserId(UUID id);

    Collection<Object> findBySpecialityId(UUID id);
}