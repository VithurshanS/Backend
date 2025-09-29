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

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty(value = "last_name", access = JsonProperty.Access.WRITE_ONLY)
    private String lastName;

    // Handle the variation with capital N
    @JsonProperty("last_Name")
    public void setLastNameVariation(String lastName) {
        this.lastName = lastName;
    }

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
