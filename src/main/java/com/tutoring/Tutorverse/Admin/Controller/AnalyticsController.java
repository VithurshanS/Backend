package com.tutoring.Tutorverse.Admin.Controller;

import com.tutoring.Tutorverse.Admin.Dto.AnalyticsOverviewDto;
import com.tutoring.Tutorverse.Admin.Services.AnaliticalService;
import com.tutoring.Tutorverse.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
public class AnalyticsController {

    @Autowired private AnaliticalService analyticsService;
    @Autowired private UserService userService;

    @GetMapping("/overview")
    public ResponseEntity<?> overview(HttpServletRequest req) {
        // Simple role check via JWT cookie
        if (!userService.hasRole(req, "ADMIN") && !userService.hasRole(req, "SUPER_ADMIN")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        AnalyticsOverviewDto dto = analyticsService.getOverview();
        return ResponseEntity.ok(dto);
    }
}
