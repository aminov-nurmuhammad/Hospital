package org.example.hospital.dto;

import java.time.LocalDate;

public record UserReq(String firstName, String lastName, String phone, String password, LocalDate birthDate, String address) {
}