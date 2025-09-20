package com.tutoring.Tutorverse.Controller;

import com.tutoring.Tutorverse.Dto.MeetingRequestDto;
import com.tutoring.Tutorverse.Services.MeetingService;
import com.tutoring.Tutorverse.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/meeting")
public class MeetingController {

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private UserService userService;

    @PostMapping("/join")
    public ResponseEntity<?> createMeeting(@RequestBody MeetingRequestDto meetingRequest, HttpServletRequest req) {
        try {
            // Extract JWT token from cookies
            String token = userService.getTokenFromRequest(req);
            if (token == null) {
                return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "error", "Invalid or missing authentication token"
                ));
            }

            // Call the meeting service to create meeting with all functionality
            Map<String, Object> response = meetingService.createMeeting(meetingRequest, token);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of(
                "success", false,
                "error", "Error creating meeting: " + e.getMessage()
            ));
        }
    }
}
