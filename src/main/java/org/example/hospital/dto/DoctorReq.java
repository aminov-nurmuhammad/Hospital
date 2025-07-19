package org.example.hospital.dto;

import java.time.LocalDate;

public record DoctorReq(String firstName, String lastName, String phone, String password, String address, LocalDate birthDate) {
}