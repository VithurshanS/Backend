package com.tutoring.Tutorverse.Controller;


import com.tutoring.Tutorverse.Services.EnrollmentService;
import com.tutoring.Tutorverse.Services.EmailNotificationService;
import com.tutoring.Tutorverse.Services.NotificationSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.tutoring.Tutorverse.Services.SendgridService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/emails")
public class EmailController {

    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private SendgridService sendgridService;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    @Autowired
    private NotificationSchedulerService schedulerService;

    @GetMapping("/enrolled")
    public List<String> getStudentEmails(@RequestParam("module_id") UUID moduleId) {
        return enrollmentService.getStudentEmailsByModuleId(moduleId);
    }
    
    /**
     * Test SendGrid configuration by sending a simple test email
     */
//    @PostMapping("/test-sendgrid")
//    public ResponseEntity<String> testSendGrid(@RequestParam String toEmail) {
//        try {
//            sendgridService.sendReminderEmail(
//                toEmail,
//                "Test Course",
//
//                LocalDateTime.now().plusHours(1)
//            );
//            return ResponseEntity.ok("✅ Test email sent successfully to " + toEmail);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("❌ Failed to send email: " + e.getMessage());
//        }
//    }
    

    @PostMapping("/test-notification")
    public ResponseEntity<String> testNotification(
            @RequestParam UUID scheduleId,
            @RequestParam UUID moduleId,
            @RequestParam String courseName,
            @RequestParam String scheduledTime,
            @RequestParam String scheduledDate) {
        try {
            emailNotificationService.sendScheduleNotification(scheduleId, moduleId, courseName, scheduledTime, scheduledDate);
            return ResponseEntity.ok("Schedule notification sent for ID: " + scheduleId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send notification: " + e.getMessage());
        }
    }
    
    /**
     * Manually trigger the scheduler (for testing)
     */
    @PostMapping("/trigger-scheduler")
    public ResponseEntity<String> triggerScheduler() {
        try {
            schedulerService.checkSchedulesAndNotify();
            return ResponseEntity.ok("Scheduler triggered manually");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to trigger scheduler: " + e.getMessage());
        }
    }
    
    /**
     * Get basic scheduler info (simplified)
     */
    @GetMapping("/scheduler-info")
    public ResponseEntity<String> getSchedulerInfo() {
        return ResponseEntity.ok("Scheduler is running every 5 minutes. Check application logs for detailed statistics.");
    }

}
