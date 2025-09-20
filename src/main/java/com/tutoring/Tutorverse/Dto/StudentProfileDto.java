package com.tutoring.Tutorverse.Dto;

import java.time.LocalDate; 
import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfileDto {
    
    private UUID studentId;  
    private String name;
    private LocalDate birthday;
    private String imageUrl; 
    private LocalDate lastAccessed;
    private Boolean isActive;
    private String phoneNumber;
    private String bio;

}
