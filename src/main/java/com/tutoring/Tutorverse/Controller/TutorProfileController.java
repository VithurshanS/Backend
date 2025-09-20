package com.tutoring.Tutorverse.Controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.tutoring.Tutorverse.Dto.TutorProfileDto;
import com.tutoring.Tutorverse.Model.TutorEntity;
import com.tutoring.Tutorverse.Services.TutorProfileService;
import com.tutoring.Tutorverse.Services.UserService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;




@RestController
@RequestMapping("/api/tutor-profile")
public class TutorProfileController {


    @Autowired
    private TutorProfileService tutorProfileService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createTutorProfile(@RequestBody TutorProfileDto dto, HttpServletRequest req) {
        try {
            // if (authHeader == null || authHeader.isBlank()) {
            //     return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing Authorization header");
            // }
            //String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
            // Extract userId (UUID) from JWT and override any provided tutorId in the body
            UUID userId = userService.getUserIdFromRequest(req);
            dto.setTutorId(userId);
            TutorEntity createdProfile = tutorProfileService.createTutorProfile(dto);
            return ResponseEntity.ok(createdProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }


    @GetMapping("/all")
    public ResponseEntity<List<TutorEntity>> getAllTutorProfiles() {
        List<TutorEntity> profiles = tutorProfileService.getAllTutorProfiles();
        return ResponseEntity.ok(profiles);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getTutorProfile(HttpServletRequest req) {
        try {
            // Extract userId from JWT cookie
            UUID userId = userService.getUserIdFromRequest(req);
            
            // Check if userId is null (invalid/missing token)
            if (userId == null) {
                System.out.println("=== TUTOR PROFILE /me ENDPOINT ===");
                System.out.println("Authentication failed: No valid JWT token found in request");
                System.out.println("Cookies present: " + (req.getCookies() != null ? req.getCookies().length : 0));
                if (req.getCookies() != null) {
                    for (jakarta.servlet.http.Cookie cookie : req.getCookies()) {
                        System.out.println("Cookie: " + cookie.getName() + " = " + 
                            (cookie.getValue() != null ? cookie.getValue().substring(0, Math.min(20, cookie.getValue().length())) + "..." : "null"));
                    }
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required: Invalid or missing JWT token");
            }
            
            System.out.println("=== TUTOR PROFILE /me ENDPOINT ===");
            System.out.println("Authenticated user ID: " + userId);
            
            TutorEntity tutorProfile = tutorProfileService.getTutorProfile(userId);
            return ResponseEntity.ok(tutorProfile);
        } catch (Exception e) {
            System.out.println("=== TUTOR PROFILE /me ENDPOINT ERROR ===");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving tutor profile: " + e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<?> updateTutorProfile(@RequestBody TutorProfileDto dto, HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }
            dto.setTutorId(userId); // Ensure the DTO tutorId matches token subject
            TutorEntity updatedProfile = tutorProfileService.updateTutorProfile(userId, dto);
            return ResponseEntity.ok(updatedProfile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteTutorProfile(HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }
            tutorProfileService.deleteTutorProfile(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }
            String newPassword = body.get("newPassword");
            if (newPassword == null || newPassword.isBlank()) {
                return ResponseEntity.badRequest().body("newPassword is required");
            }
            tutorProfileService.changePassword(userId, newPassword);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<TutorProfileDto>> searchTutorProfiles(@RequestParam String query) {
        List<TutorProfileDto> results = tutorProfileService.searchTutorProfiles(query);
        return ResponseEntity.ok(results);
    }
    

}
