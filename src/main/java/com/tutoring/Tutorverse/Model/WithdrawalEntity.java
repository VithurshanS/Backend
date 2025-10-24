package com.tutoring.Tutorverse.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "withdrawals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WithdrawalEntity {

    @Id
    @GeneratedValue
    @Column(name = "withdrawal_id")
    private UUID withdrawalId;

    @Column(name = "tutor_id", nullable = false)
    private UUID tutorId;

    @Column(name = "tutor_name", nullable = false)
    private String tutorName;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING";
    // PENDING: tutor requested
    // APPROVED: admin approved and ready to pay
    // REJECTED: admin declined
    // PAID: payment completed

    @Column(name = "method", nullable = false)
    private String method; // e.g., BANK, EZCASH, PAYHERE

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "bank_name")
    private String bankName; // optional if method != BANK

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    // ✅ New fields below (for admin actions & PayHere)

    @Column(name = "admin_id")
    private UUID adminId;
    // Which admin approved or rejected this withdrawal

    @Column(name = "transaction_id")
    private String transactionId;
    // PayHere transaction reference ID

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    // When PayHere confirmed payment
}

