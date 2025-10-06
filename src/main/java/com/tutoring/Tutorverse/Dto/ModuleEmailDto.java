package com.tutoring.Tutorverse.Dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuleEmailDto {
    private String email;
    private String userType; // "TUTOR" or "STUDENT"
    private UUID userId;
    private String userName;
}