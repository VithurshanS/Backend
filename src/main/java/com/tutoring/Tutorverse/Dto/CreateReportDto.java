
package com.tutoring.Tutorverse.Dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateReportDto {
	private UUID moduleId;
	private String reason;
	private UUID reportedBy;
}
