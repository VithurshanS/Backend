package com.tutoring.Tutorverse.Model;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;

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

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "image_url", nullable = true)
    private String imageUrl;

    @Column(name = "last_accessed", nullable = true)
    private LocalDate lastAccessed;

    @Column(name = "is_active", nullable = true)
    private Boolean isActive;

    @Column(name = "phone_number", nullable = false, unique = true)
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
        validateUserRole();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validateUserRole();
    }

    /**
     * Validates that the associated user has the STUDENT role
     * Throws IllegalArgumentException if the user role is not STUDENT
     */
    private void validateUserRole() {
        if (user != null && user.getRole() != null) {
            String roleName = user.getRole().getName();
            if (!"STUDENT".equals(roleName)) {
                throw new IllegalArgumentException(
                    "StudentEntity can only be associated with users having STUDENT role. Found role: " + roleName
                );
            }
        } else if (user != null && user.getRole() == null) {
            throw new IllegalArgumentException("User must have a role assigned to be associated with StudentEntity");
        } else if (user == null) {
            throw new IllegalArgumentException("StudentEntity must be associated with a User");
        }
    }

    /**
     * Bean validation method to ensure user role is STUDENT
     * This will be called during validation
     */
    @AssertTrue(message = "User must have STUDENT role to be associated with StudentEntity")
    public boolean isUserRoleValid() {
        if (user == null || user.getRole() == null) {
            return false; // User and role must be present
        }
        return "STUDENT".equals(user.getRole().getName());
    }
}
