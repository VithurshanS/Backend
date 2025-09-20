package com.tutoring.Tutorverse.Dto;

import java.time.LocalDate;
import java.util.UUID;
import com.tutoring.Tutorverse.Model.TutorEntity;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TutorProfileDto {
    private UUID tutorId;
    private String firstName;
    private String lastName;
    private String phoneNo;
    private TutorEntity.Gender gender;
    private LocalDate dob;
    private String portfolio;
    private String bio;
    private String image;
    private String address;
    private String city;
    private String country;
    
}
