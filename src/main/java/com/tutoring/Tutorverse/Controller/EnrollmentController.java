package com.tutoring.Tutorverse.Controller;

import java.util.List;
import java.util.UUID;

import com.tutoring.Tutorverse.Dto.ModuelsDto;
import com.tutoring.Tutorverse.Dto.EnrollRequestDto;
import com.tutoring.Tutorverse.Services.UserService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tutoring.Tutorverse.Dto.EnrollCreateDto;
import com.tutoring.Tutorverse.Services.EnrollmentService;

@RestController
@RequestMapping("/api/enrollment")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private UserService userService;

    @GetMapping("/get-enrollments")
    public ResponseEntity<List<ModuelsDto>> getEnrollments(HttpServletRequest req) {
        try {
            List<ModuelsDto> enrollments = enrollmentService.getEnrollmentByStudentId(userService.getUserIdFromRequest(req));
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }

    }

    @PostMapping("/enroll")
    public ResponseEntity<String> enrollStudent(@RequestBody EnrollRequestDto enrollRequest, HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if(userId == null || !userService.isStudent(userId)) {
                return ResponseEntity.status(401).body("Unauthorized or Invalid User");
            }
            EnrollCreateDto enrollCreateDto = new EnrollCreateDto();
            enrollCreateDto.setStudentId(userId);
            enrollCreateDto.setModuleId(enrollRequest.getModuleId());
            String message = enrollmentService.EnrollTOModule(enrollCreateDto);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error enrolling student: " + e.getMessage());
        }
    }

    @DeleteMapping("/unenroll/{enrollmentId}")
    public ResponseEntity<String> unenrollStudent(@PathVariable UUID enrollmentId, HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if(userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            enrollmentService.unenrollFromModule(enrollmentId);
            return ResponseEntity.ok("Unenrolled Successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error unenrolling student: " + e.getMessage());
        }
    }

}
