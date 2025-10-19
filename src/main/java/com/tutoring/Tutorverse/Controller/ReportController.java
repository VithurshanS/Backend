package com.tutoring.Tutorverse.Controller;

import com.tutoring.Tutorverse.Dto.CreateReportDto;
import com.tutoring.Tutorverse.Dto.GetReportDto;
import com.tutoring.Tutorverse.Services.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import com.tutoring.Tutorverse.Services.UserService;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {


	@Autowired
	private ReportService reportService;

	@Autowired
	private UserService userService;

	@PostMapping
	public ResponseEntity<GetReportDto> createReport(@RequestBody CreateReportDto dto, HttpServletRequest request) {
		UUID reportedById = userService.getUserIdFromRequest(request);
		if (reportedById == null) {
			return ResponseEntity.status(401).build();
		}
		dto.setReportedBy(reportedById);
		GetReportDto created = reportService.createReport(dto);
		return ResponseEntity.ok(created);
	}

	@GetMapping
	public ResponseEntity<List<GetReportDto>> getAllReports() {
		List<GetReportDto> reports = reportService.getAllReports();
		return ResponseEntity.ok(reports);
	}

	@PutMapping("/review")
	public ResponseEntity<Void> reviewReport(@RequestParam UUID reportId) {
		reportService.reviewReport(reportId);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/resolve")
	public ResponseEntity<Void> resolveReport(@RequestParam UUID reportId) {
		reportService.resolveReport(reportId);
		return ResponseEntity.noContent().build();
	}
}
