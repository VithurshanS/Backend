package com.tutoring.Tutorverse.Model;
import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "student")
@Data 
@NoArgsConstructor
@AllArgsConstructor
@Builder 

public class StudentEntity {

    @Id
    @Column(name = "student_id", nullable = false)
    private UUID studentId; 

    @OneToOne
    @MapsId
    @JoinColumn(name = "student_id") 
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "image_url", nullable = true)
    private String imageUrl;

    @Column(name = "last_accessed", nullable = true)
    private LocalDate lastAccessed;

    @Column(name = "is_active", nullable = true)
    private Boolean isActive;

    @Column(name = "phone_number", nullable = true)
    private String phoneNumber;

    @Column(name = "bio", nullable = true, columnDefinition = "TEXT")
    private String bio;

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
}
