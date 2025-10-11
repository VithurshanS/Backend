package com.tutoring.Tutorverse.Admin.Controller;

import com.tutoring.Tutorverse.Admin.Dto.*;
import com.tutoring.Tutorverse.Admin.Services.AnaliticalService;
import com.tutoring.Tutorverse.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
public class AnalyticsController {

    @Autowired private AnaliticalService analyticsService;
    @Autowired private UserService userService;

    private boolean isAuthorized(HttpServletRequest req) {
        return userService.hasRole(req, "ADMIN") || userService.hasRole(req, "SUPER_ADMIN");
    }


    @GetMapping("/users")
    public ResponseEntity<?> users(HttpServletRequest req) {
        if (!isAuthorized(req)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        UsersSummaryDto dto = analyticsService.getUsersSummary();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/students")
    public ResponseEntity<?> students(HttpServletRequest req) {
        if (!isAuthorized(req)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        StudentsSummaryDto dto = analyticsService.getStudentsSummary();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/tutors")
    public ResponseEntity<?> tutors(HttpServletRequest req) {
        if (!isAuthorized(req)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        TutorsSummaryDto dto = analyticsService.getTutorsSummary();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/modules")
    public ResponseEntity<?> modules(HttpServletRequest req) {
        if (!isAuthorized(req)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        ModulesSummaryDto dto = analyticsService.getModulesSummary();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/enrollments")
    public ResponseEntity<?> enrollments(HttpServletRequest req) {
        if (!isAuthorized(req)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        long count = analyticsService.getEnrollmentsCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/revenue")
    public ResponseEntity<?> revenue(HttpServletRequest req) {
        if (!isAuthorized(req)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        RevenueSummaryDto dto = analyticsService.getRevenueSummary();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/ratings")
    public ResponseEntity<?> ratings(HttpServletRequest req) {
        if (!isAuthorized(req)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        RatingsSummaryDto dto = analyticsService.getRatingsSummary();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/schedules")
    public ResponseEntity<?> schedules(HttpServletRequest req) {
        if (!isAuthorized(req)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        SchedulesSummaryDto dto = analyticsService.getSchedulesSummary();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/top-modules")
    public ResponseEntity<?> topModules(HttpServletRequest req, @RequestParam(defaultValue = "5") int limit) {
        if (!isAuthorized(req)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        TopModulesDto dto = analyticsService.getTopModulesByRevenue(Math.max(1, Math.min(limit, 20)));
        return ResponseEntity.ok(dto);
    }
}
