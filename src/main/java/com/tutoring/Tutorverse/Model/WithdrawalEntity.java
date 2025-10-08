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
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED, PAID

    @Column(name = "method", nullable = false)
    private String method; // e.g., "BANK", "EZCASH", etc.

    @Column(name = "account_name", nullable = false)
    private String accountName;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "processed_at")
    private LocalDateTime processedAt;
}
