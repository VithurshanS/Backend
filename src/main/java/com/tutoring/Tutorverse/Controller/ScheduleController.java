package com.tutoring.Tutorverse.Controller;

import com.tutoring.Tutorverse.Dto.ScheduleDto;
import com.tutoring.Tutorverse.Services.ScheduleService;
import com.tutoring.Tutorverse.Services.JwtServices;
import com.tutoring.Tutorverse.Repository.userRepository;
import com.tutoring.Tutorverse.Repository.ModulesRepository;
import com.tutoring.Tutorverse.Model.User;
import com.tutoring.Tutorverse.Model.ModuelsEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private JwtServices jwtServices;

    @Autowired
    private userRepository userRepo;

    @Autowired
    private ModulesRepository modulesRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createSchedule(@RequestBody ScheduleDto scheduleDto,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User tutor = requireTutor(authHeader);

            // Verify that the module belongs to the authenticated tutor
            Optional<ModuelsEntity> moduleOpt = modulesRepository.findById(scheduleDto.getModuleId());
            if (moduleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found");
            }

            ModuelsEntity module = moduleOpt.get();
            if (!module.getTutorId().equals(tutor.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only create schedules for your own modules");
            }

            ScheduleDto createdSchedule = scheduleService.createSchedule(scheduleDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to create schedule");
        }
    }

    @GetMapping
    public ResponseEntity<List<ScheduleDto>> getAllSchedules() {
        List<ScheduleDto> schedules = scheduleService.getAllSchedules();
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByModule(@PathVariable UUID moduleId) {
        List<ScheduleDto> schedules = scheduleService.getSchedulesByModuleId(moduleId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<ScheduleDto>> getSchedulesByTutor(@PathVariable UUID tutorId) {
        List<ScheduleDto> schedules = scheduleService.getSchedulesByTutorId(tutorId);
        return ResponseEntity.ok(schedules);
    }

    @GetMapping("/my-schedules")
    public ResponseEntity<?> getMySchedules(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User tutor = requireTutor(authHeader);
            List<ScheduleDto> schedules = scheduleService.getSchedulesByTutorId(tutor.getId());
            return ResponseEntity.ok(schedules);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<?> updateSchedule(@PathVariable UUID scheduleId,
                                          @RequestBody ScheduleDto scheduleDto,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User tutor = requireTutor(authHeader);

            // Verify ownership through module
            Optional<ModuelsEntity> moduleOpt = modulesRepository.findById(scheduleDto.getModuleId());
            if (moduleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found");
            }

            ModuelsEntity module = moduleOpt.get();
            if (!module.getTutorId().equals(tutor.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only update schedules for your own modules");
            }

            ScheduleDto updatedSchedule = scheduleService.updateSchedule(scheduleId, scheduleDto);
            return ResponseEntity.ok(updatedSchedule);

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update schedule");
        }
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<?> deleteSchedule(@PathVariable UUID scheduleId,
                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User tutor = requireTutor(authHeader);

            // Here you would need to verify ownership, but for simplicity, we'll allow any tutor to delete
            // In production, you should verify the schedule belongs to the tutor

            scheduleService.deleteSchedule(scheduleId);
            return ResponseEntity.noContent().build();

        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete schedule");
        }
    }

    // Test endpoint specifically for conflict testing
    @PostMapping("/test-conflict")
    public ResponseEntity<?> testScheduleConflict(@RequestBody ScheduleDto scheduleDto,
                                                @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            User tutor = requireTutor(authHeader);

            // Verify module ownership
            Optional<ModuelsEntity> moduleOpt = modulesRepository.findById(scheduleDto.getModuleId());
            if (moduleOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Module not found");
            }

            ModuelsEntity module = moduleOpt.get();
            if (!module.getTutorId().equals(tutor.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only test schedules for your own modules");
            }

            // Try to create the schedule - if it conflicts, it will throw an exception
            ScheduleDto createdSchedule = scheduleService.createSchedule(scheduleDto);
            return ResponseEntity.ok(Map.of(
                "message", "No conflict detected - schedule created successfully",
                "schedule", createdSchedule
            ));

        } catch (ResponseStatusException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "conflict", true,
                    "message", e.getReason(),
                    "conflictType", "Schedule overlap detected"
                ));
            }
            return ResponseEntity.status(e.getStatusCode()).body(Map.of(
                "error", true,
                "message", e.getReason()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", true,
                "message", "Failed to test schedule conflict"
            ));
        }
    }

//    // Test endpoint to call the find_matching_schedule database function
//    @GetMapping("/test-matching-schedule")
//    public ResponseEntity<?> testFindMatchingSchedule() {
//        try {
//            UUID result = scheduleService.testFindMatchingSchedule();
//            return ResponseEntity.ok(Map.of(
//                "message", "find_matching_schedule function called successfully",
//                "parameters", Map.of(
//                    "reqDate", "2025-09-10",
//                    "reqTime", "10:30:00",
//                    "moduleId", "6082f12a-2859-4ae5-93df-920ff6804fcf"
//                ),
//                "result", result
//            ));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(Map.of("error", "Failed to call find_matching_schedule function",
//                           "message", e.getMessage()));
//        }
//    }

    private User requireTutor(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid authorization token");
        }

        String token = authHeader.substring(7);
        if (!jwtServices.validateJwtToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }

        String email = jwtServices.getEmailFromJwtToken(token);
        Optional<User> userOpt = userRepo.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
        }

        User user = userOpt.get();
        if (!"TUTOR".equals(user.getRole().getName())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only tutors can manage schedules");
        }

        return user;
    }
}
