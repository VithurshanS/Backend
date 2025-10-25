package com.tutoring.Tutorverse.Controller;

import com.tutoring.Tutorverse.Dto.RatingCreateDto;
import com.tutoring.Tutorverse.Dto.RatingGetDto;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Repository.userRepository;
import com.tutoring.Tutorverse.Services.JwtServices;
import com.tutoring.Tutorverse.Services.RatingService;
import com.tutoring.Tutorverse.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtServices jwtServices;

    @Autowired
    private userRepository userRepo;

    @PostMapping("/create")
    public ResponseEntity<String> createRating(@Valid @RequestBody RatingCreateDto ratingCreateDto,
                                               @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid Authorization header");
            }
            String token = authHeader.substring(7);
            if (!jwtServices.validateJwtToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }
            UUID userId = jwtServices.getUserIdFromJwtToken(token);
            // Fetch user to get name
            User user = userRepo.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
            }
            // Overwrite any client-provided studentName to avoid spoofing
            ratingCreateDto.setStudentName(user.getName() != null ? user.getName() : "");

            String message = ratingService.createRating(ratingCreateDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error creating rating: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/{enrolmentId}")
    public ResponseEntity<RatingGetDto> getRatingByEnrollmentId(@PathVariable UUID enrolmentId,
                                                               HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            RatingGetDto rating = ratingService.getRatingByEnrollmentId(enrolmentId);
            return ResponseEntity.ok(rating);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/module")
    public ResponseEntity<List<RatingGetDto>> getRatingsByModuleId(@RequestParam UUID moduleId) {
        try {
            List<RatingGetDto> ratings = ratingService.getRatingsByModuleId(moduleId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<RatingGetDto>> getRatingsByStudentId(@PathVariable UUID studentId,
                                                                   HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            List<RatingGetDto> ratings = ratingService.getRatingsByStudentId(studentId);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{enrolmentId}")
    public ResponseEntity<String> deleteRating(@PathVariable UUID enrolmentId,
                                               HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Unauthorized: Invalid user");
            }

            ratingService.deleteRating(enrolmentId);
            return ResponseEntity.ok("Rating deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error deleting rating: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }

    @GetMapping("/exists/{enrolmentId}")
    public ResponseEntity<Boolean> hasRatingForEnrollment(@PathVariable UUID enrolmentId,
                                                         HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(false);
            }

            boolean hasRating = ratingService.hasRatingForEnrollment(enrolmentId);
            return ResponseEntity.ok(hasRating);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }
}
