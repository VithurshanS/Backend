package com.tutoring.Tutorverse.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletEntity {
        @Id
        @Column(name = "tutor_id", nullable = false, unique = true)
        private UUID tutorId;

        @Column(name = "available_balance", nullable = false)
        @Builder.Default
        private double availableBalance = 0.0;

        @Column(name = "updated_at")
        @Builder.Default
        private LocalDateTime updatedAt = LocalDateTime.now();
}
