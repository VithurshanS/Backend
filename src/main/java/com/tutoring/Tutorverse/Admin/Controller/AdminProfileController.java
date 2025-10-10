package com.tutoring.Tutorverse.Admin.Controller;

import com.tutoring.Tutorverse.Admin.Dto.AdminProfileDto;
import com.tutoring.Tutorverse.Admin.Model.AdminProfileEntity;
import com.tutoring.Tutorverse.Admin.Services.AdminProfileService;
import com.tutoring.Tutorverse.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin-profile")
public class AdminProfileController {

    @Autowired
    private AdminProfileService adminService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createOrUpdate(@RequestBody AdminProfileDto dto, HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }
            dto.setAdminId(userId); // enforce
            AdminProfileEntity saved = adminService.createOrUpdateAdminProfile(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }
            return ResponseEntity.ok(adminService.getAdminProfile(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> delete(HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }
            adminService.deleteAdminProfile(userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAdminExists(HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }
            adminService.checkAdminExists(userId);
            return ResponseEntity.ok("True");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }


    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestParam String newPassword, HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            }
            adminService.changePassword(userId, newPassword);
            return ResponseEntity.ok("Password changed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllAdminProfiles() {
        try {
            return ResponseEntity.ok(adminService.getAllAdminProfiles());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
