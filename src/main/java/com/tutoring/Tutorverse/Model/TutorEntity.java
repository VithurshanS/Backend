package com.tutoring.Tutorverse.Model;

import java.time.LocalDate;
import java.util.UUID;
import lombok.*;
import jakarta.persistence.*;



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
    private userDto user;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "phone_no", nullable = false, unique = true)
    private String phoneNo;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "last_accessed", nullable = true)
    private LocalDate lastAccessed;

    @Column(name = "image", nullable = true)
    private String image;

    @Column(name = "portfolio")
    private String portfolio;

    @Column(name = "bio", nullable = true)
    private String bio;

    public enum Gender {
        MALE, FEMALE, OTHER
    }


}
