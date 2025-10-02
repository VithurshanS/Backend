package com.tutoring.Tutorverse.Dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingCreateDto {
    
    @NotNull(message = "Enrollment ID is required")
    private UUID enrolmentId;
    
    @NotNull(message = "Rating is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Rating must be between 0.0 and 5.0")
    @DecimalMax(value = "5.0", inclusive = true, message = "Rating must be between 0.0 and 5.0")
    private BigDecimal rating;
    
    @Size(max = 1000, message = "Feedback cannot exceed 1000 characters")
    private String feedback;
    
    private String studentName;
}
