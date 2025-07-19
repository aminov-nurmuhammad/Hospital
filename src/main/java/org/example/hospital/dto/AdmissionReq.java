package org.example.hospital.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdmissionReq(String description, String admissionDateTime) {
}