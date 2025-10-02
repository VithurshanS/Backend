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
import com.tutoring.Tutorverse.Services.JwtServices;


@RestController
@RequestMapping("/api/enrollment")
public class EnrollmentController {

    @Autowired
	private JwtServices jwtServices;

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


    @GetMapping("/getenrollmentid")
    public ResponseEntity<String> getEnrollmentId(@RequestParam String Module_Id,@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body("Missing or invalid Authorization header");
            }
            String token = authHeader.substring(7);
            if (!jwtServices.validateJwtToken(token)) {
                return ResponseEntity.badRequest().body("Invalid token");
            }
            UUID userId = jwtServices.getUserIdFromJwtToken(token);
            // Fetch user to get name
            UUID enrollmentId = enrollmentService.getEnrollmentId(userId,UUID.fromString(Module_Id));
            if (enrollmentId == null) {
                return ResponseEntity.status(404).body("Enrollment not found");
            }
            return ResponseEntity.ok(enrollmentId.toString());
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body("Error fetching enrollment ID: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }
    

    @GetMapping("/get-enrollment-details/{moduleId}")
    public ResponseEntity<Boolean> getEnrollmentDetails(@PathVariable UUID moduleId, HttpServletRequest req) {
        try {
            boolean isPaid = enrollmentService.findIsPaidByStudentIdAndModuleId(
                    userService.getUserIdFromRequest(req),
                    moduleId
            );
            return ResponseEntity.ok(isPaid);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(false);
        }
    }



}
