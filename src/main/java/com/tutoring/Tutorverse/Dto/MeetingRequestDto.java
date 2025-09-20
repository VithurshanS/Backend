package com.tutoring.Tutorverse.Dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingRequestDto {
    private UUID moduleId;
    private LocalDate requestedDate;
    private LocalTime requestedTime;
}
