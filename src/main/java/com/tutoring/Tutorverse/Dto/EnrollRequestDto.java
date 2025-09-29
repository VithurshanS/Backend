package com.tutoring.Tutorverse.Dto;

import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollRequestDto {
    private UUID moduleId;

}
