package com.tutoring.Tutorverse.Model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {

    @Id
    @GeneratedValue
    @Column(name = "payment_id", nullable = false, updatable = false)
    private UUID paymentId;

    @Column(name = "student_id", nullable = false)
    private UUID studentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", referencedColumnName = "student_id", insertable = false, updatable = false)
    private StudentEntity student;

    @Column(name = "module_id", nullable = false)
    private UUID moduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", referencedColumnName = "module_id", insertable = false, updatable = false)
    private ModuelsEntity module;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false, length = 10)
    private String currency = "LKR";

    @Column(name = "order_id", nullable = false, unique = true)
    private String orderId;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "payhere_payment_id")
    private String payherePaymentId;

    @Column(name = "payhere_signature")
    private String payhereSignature;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
