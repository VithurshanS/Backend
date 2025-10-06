package com.tutoring.Tutorverse.Model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private UUID tutorName;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "method", nullable = false)
    private String method;

    @Column(name = "account_details")
    private String accountDetails;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

}
