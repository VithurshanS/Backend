package com.tutoring.Tutorverse.Model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Duration;
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

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain", referencedColumnName = "domain_id")
    private DomainEntity domain;

    @Column(name = "average_ratings", precision = 3, scale = 1)
    private BigDecimal averageRatings = BigDecimal.valueOf(0.0);

    @Column(name = "fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal fee;

    @Column(name = "duration")
    private Duration duration;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private ModuleStatus status;

    public enum ModuleStatus {
        Draft, Active, Archived
    }

}






