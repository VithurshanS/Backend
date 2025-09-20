package com.tutoring.Tutorverse.Model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "enrollment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollmentEntity {

    @Id
    @GeneratedValue
    @Column(name = "enrolment_id", updatable = false, nullable = false)
    private UUID enrolmentId;

    // Many Enrollments -> One Student
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    // Many Enrollments -> One Module
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private ModuelsEntity module;

    @Column(name = "is_paid", nullable = false)
    private boolean isPaid;
}
