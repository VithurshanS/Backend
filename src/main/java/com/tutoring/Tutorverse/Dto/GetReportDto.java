package com.tutoring.Tutorverse.Dto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetReportDto {
    private UUID reportId;
    private String moduleName;
    private String reportedBy;
    private String reason;
    private String reportDate;
    private String status;

}
