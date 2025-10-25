package com.tutoring.Tutorverse.Admin.Dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementGetDto {
    private UUID id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private boolean isActive;
}
