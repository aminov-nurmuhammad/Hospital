 package org.example.hospital.entity;

 import jakarta.persistence.*;
 import lombok.*;
 import org.example.hospital.entity.enums.AdmissionStatus;
 import org.example.hospital.entity.enums.ProcedureStatus;
 import org.example.hospital.entity.users.Administrator;
 import org.example.hospital.entity.users.Doctor;
 import org.example.hospital.entity.users.Patient;

 import java.time.LocalDateTime;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;

@Entity
@Table(name = "admissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Admission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrator_id")
    private Administrator administrator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            name = "admission_procedures",
            joinColumns = @JoinColumn(name = "admission_id"),
            inverseJoinColumns = @JoinColumn(name = "procedure_id")
    )
    private List<Procedure> procedures = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(
            name = "admission_prescriptions",
            joinColumns = @JoinColumn(name = "admission_id"),
            inverseJoinColumns = @JoinColumn(name = "prescription_id")
    )
    private List<Prescription> prescriptions = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime admissionDateTime;

    private LocalDateTime arrivedDate;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdmissionStatus status;

    @Builder.Default
    @Column(nullable = false, columnDefinition = "double precision default 0")
    private double totalPrice = 0.0;

    @Builder.Default
    private boolean cancelled = false;

    private String cancellationReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = AdmissionStatus.SCHEDULED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        this.cancelled = true;
        this.cancellationReason = reason;
        this.status = AdmissionStatus.CANCELLED;
        for (Procedure procedure : procedures) {
            procedure.setStatus(ProcedureStatus.CANCELLED);
        }
        for (Prescription prescription : prescriptions) {
            prescription.cancel("Cancelled due to admission cancellation");
        }
    }
}