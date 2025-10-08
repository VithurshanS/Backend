package com.tutoring.Tutorverse.Admin.Dto;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProfileDto {
    private UUID adminId; // same as user id
    private String fullName;
    private String email;
    private String contactNumber;
    private String bio;
    private String imageUrl; // profile picture
}
