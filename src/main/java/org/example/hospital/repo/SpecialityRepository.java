package org.example.hospital.repo;

import org.example.hospital.entity.Speciality;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpecialityRepository extends JpaRepository<Speciality, UUID> {
    Optional<Object> findByName(String name);
}