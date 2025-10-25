package com.tutoring.Tutorverse.Admin.Model;

import com.tutoring.Tutorverse.Model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "admin_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProfileEntity {

    @Id
    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "admin_id")
    private User user;

    @Column(name = "full_name", nullable = false)
    private String fullName; // display name / merged first + last

    @Column(name = "email", nullable = false)
    private String email; // duplicate for faster queries / denormalized

    @Column(name = "contact_number")
    private String contactNumber;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "image_url")
    private String imageUrl;

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
