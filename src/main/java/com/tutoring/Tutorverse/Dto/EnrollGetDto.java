
package com.tutoring.Tutorverse.Dto;
import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrollGetDto {
    private UUID enrollmentId;
    private UUID studentId;
    private UUID moduleId;
    private boolean isPaid;
}
