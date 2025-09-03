package com.tutoring.Tutorverse.Dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDto {
    
    private UUID scheduleId;
    
    private UUID moduleId;
    
    private LocalDate date;
    
    private LocalTime time;
    
    private Integer duration; // in minutes
    
    private Integer weekNumber; // 0 = specific date, 1-7 = weekly (Mon-Sun), 8 = daily
    
    private String recurrentType; // "Weekly", "Daily", or null for one-time
    
    // Response only fields
    private String moduleName;
    private String tutorName;
    
    // Helper method to validate schedule data
    public boolean isValid() {
        return moduleId != null && 
               date != null && 
               time != null && 
               duration != null && 
               duration > 0 &&
               weekNumber != null &&
               weekNumber >= 0 && 
               weekNumber <= 8;
    }
    
    // Helper method to determine schedule type
    public String getScheduleType() {
        if (weekNumber == 0) return "One-time";
        if (weekNumber >= 1 && weekNumber <= 7) return "Weekly";
        if (weekNumber == 8) return "Daily";
        return "Unknown";
    }
}
