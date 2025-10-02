
package com.tutoring.Tutorverse.Dto;
import lombok.Data;
import lombok.*;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModuelsDto {

    private UUID moduleId;
    private UUID tutorId;
    private String name;
    private String domain;
    private BigDecimal averageRatings;
    private BigDecimal fee;
    private Duration duration;
    private String status;

}
