package com.tutoring.Tutorverse.Dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingGetDto {
    
    private UUID enrolmentId;
    private BigDecimal rating;
    private String feedback;
    private String studentName;
    private Instant createdAt;
}