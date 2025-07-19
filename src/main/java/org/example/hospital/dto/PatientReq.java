package org.example.hospital.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientReq {
    private String firstName;
    private String lastName;
    private String phone;
    private String password;
    String address;
    LocalDate birthDate;
}