package com.tutoring.Tutorverse.Services;

import com.tutoring.Tutorverse.Model.Report;
import com.tutoring.Tutorverse.Dto.CreateReportDto;
import com.tutoring.Tutorverse.Dto.GetReportDto;
import com.tutoring.Tutorverse.Model.Report.ReportStatus;
import com.tutoring.Tutorverse.Repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.tutoring.Tutorverse.Services.ModulesService;
import com.tutoring.Tutorverse.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

	@Autowired
	private ReportRepository reportRepository;

    @Autowired
    private ModulesService modulesService;

	@Autowired
	private UserService userService;

	public GetReportDto createReport(CreateReportDto dto) {
		Report report = new Report();
		report.setModuleId(dto.getModuleId());
		report.setReason(dto.getReason());
		report.setReportedBy(dto.getReportedBy());
		report.setReportDate(LocalDateTime.now());
		report.setStatus(ReportStatus.PENDING);
		report = reportRepository.save(report);
		return mapToGetReportDto(report);
	}

	public List<GetReportDto> getAllReports() {
		List<Report> reports = reportRepository.findAll();
		return reports.stream()
			.map(this::mapToGetReportDto)
			.collect(Collectors.toList());
	}

	private GetReportDto mapToGetReportDto(Report report) {
		String moduleName = null;
		if (report.getModuleId() != null) {
			try {
				moduleName = modulesService.getModuleNameById(report.getModuleId());
			} catch (Exception e) {
				moduleName = null;
			}
		}
		String reportedByName = null;
		if (report.getReportedBy() != null) {
			try {
				reportedByName = userService.getUserNameById(report.getReportedBy());
			} catch (Exception e) {
				reportedByName = null;
			}
		}
		return new GetReportDto(
			moduleName,
			reportedByName,
			report.getReason(),
			report.getReportDate().toString(),
			report.getStatus().name()
		);
	}

    public void reviewReport(UUID reportId) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(ReportStatus.REVIEWED);
        reportRepository.save(report);
    }

    public void resolveReport(UUID reportId) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(ReportStatus.RESOLVED);
        reportRepository.save(report);
    }
}

