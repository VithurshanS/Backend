package com.tutoring.Tutorverse.Admin.Controller;

import com.tutoring.Tutorverse.Admin.Dto.AnnouncementCreateDto;
import com.tutoring.Tutorverse.Admin.Dto.AnnouncementUpdateDto;
import com.tutoring.Tutorverse.Admin.Services.AnnouncementService;
import com.tutoring.Tutorverse.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/announcements")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserService userService;

    // Create announcement (requires ADMIN role ideally)
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody AnnouncementCreateDto dto, HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            // Use user name as author if available; fall back to ID string
            String author = userService.getUserFromRequest(req) != null ? userService.getUserFromRequest(req).getName() : userId.toString();
            return ResponseEntity.status(HttpStatus.CREATED).body(announcementService.create(dto, author));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Get single announcement
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable("id") UUID id) {
        try {
            return ResponseEntity.ok(announcementService.getById(id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // List announcements; ?onlyActive=true to filter
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(value = "onlyActive", required = false) Boolean onlyActive) {
        try {
            return ResponseEntity.ok(announcementService.list(onlyActive));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") UUID id,
                                    @Valid @RequestBody AnnouncementUpdateDto dto,
                                    HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            return ResponseEntity.ok(announcementService.update(id, dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id, HttpServletRequest req) {
        try {
            UUID userId = userService.getUserIdFromRequest(req);
            if (userId == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or missing authentication token");
            announcementService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/authorannouncements")
    public ResponseEntity<?> getByAuthor(HttpServletRequest req) {
        try {
            String author = userService.getUserFromRequest(req) != null ? userService.getUserFromRequest(req).getName() : null;
            return ResponseEntity.ok(announcementService.getByAuthor(author));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
