package com.tutoring.Tutorverse.Dto;

import java.time.LocalDate;
import java.util.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tutoring.Tutorverse.Model.TutorEntity;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TutorProfileDto {
    private UUID tutorId;

    @JsonProperty("firstName")
    private String firstName;

    @JsonProperty("lastName")
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
