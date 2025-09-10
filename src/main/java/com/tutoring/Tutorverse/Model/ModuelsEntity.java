package com.tutoring.Tutorverse.Model;

import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder.In;
import lombok.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "modules")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuelsEntity {

    @Id
    @GeneratedValue
    @Column(name = "module_id", nullable = false, updatable = false)
    private UUID moduleId;

    @Column(name = "tutor_id", nullable = false)
    private UUID tutorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", referencedColumnName = "tutor_id", insertable = false, updatable = false)
    private TutorEntity tutor;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain", referencedColumnName = "domain_id")
    private DomainEntity domain;

    @Column(name = "average_ratings", precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal averageRatings = BigDecimal.valueOf(0.0);

    @Column(name = "fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(name = "duration")
    private Duration duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ModuleStatus status;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ModuleStatus {
        Draft, Active, Archived
    }

}
