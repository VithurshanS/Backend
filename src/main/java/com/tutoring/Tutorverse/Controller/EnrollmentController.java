package com.tutoring.Tutorverse.Controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.tutoring.Tutorverse.Dto.ModuelsDto;
import com.tutoring.Tutorverse.Dto.EnrollRequestDto;
import com.tutoring.Tutorverse.Model.ModuelsEntity;
import com.tutoring.Tutorverse.Services.UserService;
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
    private EnrollmentService enrollmentService;

    @Autowired
    private JwtServices jwtServices;
    @Autowired
    private UserService userService;

    @GetMapping("/get-enrollments")
    public ResponseEntity<List<ModuelsDto>> getEnrollments(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            List<ModuelsDto> enrollments = enrollmentService.getEnrollmentByStudentId(getUserId(authHeader));
            return ResponseEntity.ok(enrollments);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }

    }

    @PostMapping("/enroll")
    public ResponseEntity<String> enrollStudent(@RequestBody EnrollRequestDto enrollRequest, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if(authHeader == null || !jwtServices.validateJwtToken(authHeader.substring(7))) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            EnrollCreateDto enrollCreateDto = new EnrollCreateDto();
            UUID userId = getUserId(authHeader);
            if(userId == null || !userService.isStudent(userId)) {
                return ResponseEntity.status(400).body("Invalid User");
            }
            enrollCreateDto.setStudentId(userId);
            enrollCreateDto.setModuleId(enrollRequest.getModuleId());
            String message = enrollmentService.EnrollTOModule(enrollCreateDto);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error enrolling student: " + e.getMessage());
        }
    }

    @DeleteMapping("/unenroll/{enrollmentId}")
    public ResponseEntity<String> unenrollStudent(@PathVariable UUID enrollmentId, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if(authHeader == null || !jwtServices.validateJwtToken(authHeader.substring(7))) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            enrollmentService.unenrollFromModule(enrollmentId);
            return ResponseEntity.ok("Unenrolled Successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error unenrolling student: " + e.getMessage());
        }
    }

    private UUID getUserId (String authHeader) {
        try{
            String token = authHeader.substring(7);
            return jwtServices.getUserIdFromJwtToken(token);
        } catch (Exception e) {
            return null;
        }
    }


}
