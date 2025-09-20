package com.tutoring.Tutorverse.Model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.math.BigDecimal;

@Entity
@Table(name = "rating")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingEntity {

    @Id
    @Column(name = "enrolment_id", nullable = false, updatable = false)
    private UUID enrolmentId;

    @OneToOne
    @MapsId  // Ensures Rating shares the same PK as Enrollment
    @JoinColumn(name = "enrolment_id")
    private EnrollmentEntity enrollment;

    // Use BigDecimal so precision/scale are meaningful to Hibernate (was Double causing 'scale has no meaning' error)
    @Column(name = "rating", precision = 2, scale = 1)
    private BigDecimal rating;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
}
