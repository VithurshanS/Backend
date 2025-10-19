package com.tutoring.Tutorverse.Dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetReportDto {
    private String moduleName;
    private String reportedBy;
    private String reason;
    private String reportDate;
    private String status;

}
