package org.example.hospital.repo;

import org.example.hospital.entity.users.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AdministratorRepository extends JpaRepository<Administrator, UUID> {
    Optional<Object> findByUserId(UUID id);
}