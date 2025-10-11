package com.tutoring.Tutorverse.Admin.Services;

import com.tutoring.Tutorverse.Admin.Dto.AnalyticsOverviewDto;
import com.tutoring.Tutorverse.Admin.Dto.ModulesSummaryDto;
import com.tutoring.Tutorverse.Admin.Dto.RatingsSummaryDto;
import com.tutoring.Tutorverse.Admin.Dto.RevenueSummaryDto;
import com.tutoring.Tutorverse.Admin.Dto.SchedulesSummaryDto;
import com.tutoring.Tutorverse.Admin.Dto.StudentsSummaryDto;
import com.tutoring.Tutorverse.Admin.Dto.TopModulesDto;
import com.tutoring.Tutorverse.Admin.Dto.TutorsSummaryDto;
import com.tutoring.Tutorverse.Admin.Dto.UsersSummaryDto;
import com.tutoring.Tutorverse.Model.ModuelsEntity;
import com.tutoring.Tutorverse.Model.TutorEntity;
import com.tutoring.Tutorverse.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnaliticalService {

	@Autowired private userRepository userRepo;
	@Autowired private StudentProfileRepository studentRepo;
	@Autowired private TutorProfileRepository tutorRepo;
	@Autowired private ModulesRepository modulesRepo;
	@Autowired private EnrollRepository enrollRepo;
	@Autowired private PaymentRepository paymentRepo;
	@Autowired private RatingRepository ratingRepo;
	@Autowired private ScheduleRepository scheduleRepo;


	// Segmented getters for smaller payloads
	public UsersSummaryDto getUsersSummary() {
		long usersTotal = userRepo.count();
		long admins = userRepo.countByRoleName("ADMIN");
		long tutors = userRepo.countByRoleName("TUTOR");
		long students = userRepo.countByRoleName("STUDENT");
		long users2fa = userRepo.countWithTwoFactor();
		return new UsersSummaryDto(usersTotal, admins, tutors, students, users2fa);
	}

	public StudentsSummaryDto getStudentsSummary() {
		long active = studentRepo.findByIsActiveTrue().size();
		long inactive = studentRepo.findByIsActiveFalse().size();
		return new StudentsSummaryDto(active, inactive);
	}

	public TutorsSummaryDto getTutorsSummary() {
		long tutorsApproved = tutorRepo.countByStatus(TutorEntity.Status.APPROVED);
		long tutorsPending = tutorRepo.countByStatus(TutorEntity.Status.PENDING);
		long tutorsBanned = tutorRepo.countByStatus(TutorEntity.Status.BANNED);
		return new TutorsSummaryDto(tutorsApproved, tutorsPending, tutorsBanned);
	}

	public ModulesSummaryDto getModulesSummary() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime last7 = now.minusDays(7);
		LocalDateTime last30 = now.minusDays(30);
		long modulesTotal = modulesRepo.count();
		long modulesActive = modulesRepo.countByStatus(ModuelsEntity.ModuleStatus.Active);
		long modulesLast30 = modulesRepo.countByCreatedAtBetween(last30, now);
		long modulesLast7 = modulesRepo.countByCreatedAtBetween(last7, now);
		return new ModulesSummaryDto(modulesTotal, modulesActive, modulesLast30, modulesLast7);
	}

	public long getEnrollmentsCount() {
		return enrollRepo.count();
	}

	public RevenueSummaryDto getRevenueSummary() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime last30 = now.minusDays(30);
		Double totalRevenue = nvl(paymentRepo.sumAmountByStatus("SUCCESS"));
		Double revenueLast30 = nvl(paymentRepo.sumAmountByStatusAndCreatedAtBetween("SUCCESS", last30, now));
		return new RevenueSummaryDto(totalRevenue, revenueLast30, buildRevenueTrend());
	}

	public RatingsSummaryDto getRatingsSummary() {
		double avg = nvl(ratingRepo.findPlatformAverageRating());
		return new RatingsSummaryDto(avg);
	}

	public SchedulesSummaryDto getSchedulesSummary() {
		long upcoming = scheduleRepo.findByDateBetween(LocalDate.now(), LocalDate.now().plusDays(30)).size();
		long enrollments = enrollRepo.count();
		return new SchedulesSummaryDto(upcoming, enrollments);
	}

	public TopModulesDto getTopModulesByRevenue(int limit) {
		return new TopModulesDto(topModulesByRevenue(limit));
	}

	private List<AnalyticsOverviewDto.RevenueTrendPoint> buildRevenueTrend() {
		LocalDateTime from = LocalDate.now().withDayOfMonth(1).minusMonths(5).atStartOfDay();
		List<Object[]> rows = paymentRepo.sumAmountByMonthSince("SUCCESS", from);
		// Ensure we include months with zero
		List<YearMonth> last6 = new ArrayList<>();
		YearMonth start = YearMonth.from(from);
		for (int i = 0; i < 6; i++) last6.add(start.plusMonths(i));

		// Map DB rows
		List<AnalyticsOverviewDto.RevenueTrendPoint> points = last6.stream().map(ym -> {
			String key = ym.toString(); // YYYY-MM
			double amt = rows.stream()
					.filter(r -> key.equals((String) r[0]))
					.map(r -> r[1] != null ? ((Number) r[1]).doubleValue() : 0.0)
					.findFirst().orElse(0.0);
			return new AnalyticsOverviewDto.RevenueTrendPoint(key, amt);
		}).collect(Collectors.toList());

		return points;
	}

	private List<AnalyticsOverviewDto.TopItem> topModulesByRevenue(int limit) {
		// Simple approach: read all modules and compute revenue via repo method per module
		return modulesRepo.findAll().stream()
				.map(m -> new AnalyticsOverviewDto.TopItem(
						m.getModuleId().toString(),
						m.getName(),
						nvl(paymentRepo.sumAmountByModuleIdAndStatus(m.getModuleId(), "SUCCESS"))
				))
				.sorted(Comparator.comparingDouble((AnalyticsOverviewDto.TopItem t) -> t.value).reversed())
				.limit(limit)
				.collect(Collectors.toList());
	}

	private double nvl(Double v) { return v == null ? 0.0 : v; }
}

