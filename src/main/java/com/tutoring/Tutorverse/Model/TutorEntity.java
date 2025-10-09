package com.tutoring.Tutorverse.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;



@Entity
@Table(name = "tutor")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TutorEntity {

    @Id
    @Column(name = "tutor_id", nullable = false, updatable = false)
    private UUID tutorId;

    @OneToOne 
    @MapsId 
    @JoinColumn(name = "tutor_id")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "phone_no", nullable = false, unique = true)
    private String phoneNo;

    @Column(name = "last_accessed", nullable = true)
    private LocalDate lastAccessed;

    @Column(name = "image", nullable = true, columnDefinition = "TEXT")
    private String image;

    @Column(name = "portfolio", columnDefinition = "TEXT")
    private String portfolio;

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
     * Validates that the associated user has the TUTOR role
     * Throws IllegalArgumentException if the user role is not TUTOR
     */
    private void validateUserRole() {
        if (user != null && user.getRole() != null) {
            String roleName = user.getRole().getName();
            if (!"TUTOR".equals(roleName)) {
                throw new IllegalArgumentException(
                    "TutorEntity can only be associated with users having TUTOR role. Found role: " + roleName
                );
            }
        } else if (user != null && user.getRole() == null) {
            throw new IllegalArgumentException("User must have a role assigned to be associated with TutorEntity");
        } else if (user == null) {
            throw new IllegalArgumentException("TutorEntity must be associated with a User");
        }
    }

    /**
     * Bean validation method to ensure user role is TUTOR
     * This will be called during validation
     */
    @AssertTrue(message = "User must have TUTOR role to be associated with TutorEntity")
    public boolean isUserRoleValid() {
        if (user == null || user.getRole() == null) {
            return false; // User and role must be present
        }
        return "TUTOR".equals(user.getRole().getName());
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }


}
