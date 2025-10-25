package com.tutoring.Tutorverse.Services;

import com.tutoring.Tutorverse.Dto.ModuleEmailDto;
import com.tutoring.Tutorverse.Repository.ModuleEmailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class EmailNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);

    @Autowired
    private ModuleEmailRepository moduleEmailRepository;
    
    @Autowired
    private SendgridService sendgridService;

    @Autowired
    private EnrollmentService enrollmentService;
    
    @Autowired
    private SmtpEmailService smtpEmailService;
    
    @org.springframework.beans.factory.annotation.Value("${email.notifications.enabled:false}")
    private boolean emailNotificationsEnabled;
    
    @org.springframework.beans.factory.annotation.Value("${sendgrid.api.key:}")
    private String sendgridApiKey;
    
    @org.springframework.beans.factory.annotation.Value("${email.use.smtp.fallback:false}")
    private boolean useSmtpFallback;

    /**
     * Get all emails (tutor and students) for a specific module
     */
    public List<ModuleEmailDto> getModuleEmails(UUID moduleId) {
        try {
            List<Object[]> rawResults = moduleEmailRepository.getModuleEmails(moduleId);
            List<ModuleEmailDto> emails = new ArrayList<>();
            
            for (Object[] row : rawResults) {
                ModuleEmailDto email = ModuleEmailDto.builder()
                        .email(row[0] != null ? row[0].toString() : null)
                        .userType(row[1] != null ? row[1].toString() : null)
                        .userId(row[2] != null ? (UUID) row[2] : null)
                        .userName(row[3] != null ? row[3].toString() : null)
                        .build();
                emails.add(email);
            }
            
            logger.info("Found {} emails for module {}", emails.size(), moduleId);
            return emails;
            
        } catch (Exception e) {
            logger.error("Error getting emails for module {}", moduleId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Send notification emails for a schedule
     */
    public void sendScheduleNotification(UUID scheduleId, UUID moduleId, String courseName, String scheduledTime, String scheduledDate) {
        try {
            // Log configuration status on first call
            if (logger.isDebugEnabled()) {
                logger.debug("📧 Email Configuration Status:");
                logger.debug("   - Notifications Enabled: {}", emailNotificationsEnabled);
                logger.debug("   - SendGrid API Key Configured: {}",
                        sendgridApiKey != null && !sendgridApiKey.trim().isEmpty() ? "Yes" : "No");
            }
            List<ModuleEmailDto> emails = getModuleEmails(moduleId);

            if (emails.isEmpty()) {
                logger.warn("No emails found for module {} (schedule {})", moduleId, scheduleId);
                return;
            }

            logger.info("📧 EMAIL NOTIFICATION TRIGGERED 📧");
            logger.info("================================");
            logger.info("Schedule ID: {}", scheduleId);
            logger.info("Module ID: {}", moduleId);
            logger.info("Course: {}", courseName);
            logger.info("Scheduled Date: {}", scheduledDate);
            logger.info("Scheduled Time: {}", scheduledTime);
            logger.info("Notification Time: {}", LocalDateTime.now());
            logger.info("Total Recipients: {}", emails.size());
            logger.info("--------------------------------");

            for (ModuleEmailDto emailDto : emails) {
                try {
                    // Convert scheduledDate and scheduledTime strings to LocalDateTime
                    LocalDate date = LocalDate.parse(scheduledDate);
                    LocalTime time = LocalTime.parse(scheduledTime);
                    LocalDateTime scheduledDateTime = LocalDateTime.of(date, time);

                    // Check if email notifications are enabled
                    if (emailNotificationsEnabled) {
                        boolean emailSent = false;
                        String emailMethod = "";

                        // Try SendGrid first
                        if (sendgridApiKey != null && !sendgridApiKey.trim().isEmpty()) {
                            try {
                                sendgridService.sendReminderEmail(
                                        emailDto.getEmail(),
                                        courseName,
                                        scheduledDateTime
                                );
                                emailSent = true;
                                emailMethod = "SendGrid";
                                logger.info("✅ [{}] Email SENT via {} for schedule ID: {} -> {} ({} - {})",
                                        LocalDateTime.now(),
                                        emailMethod,
                                        scheduleId,
                                        emailDto.getEmail(),
                                        emailDto.getUserType(),
                                        emailDto.getUserName());
                            } catch (Exception sendgridException) {
                                logger.warn(" SendGrid failed for {}: {} - Trying SMTP fallback...",
                                        emailDto.getEmail(), sendgridException.getMessage());

                                // Try SMTP fallback if enabled
                                if (useSmtpFallback) {
                                    try {
                                        smtpEmailService.sendReminderEmail(
                                                emailDto.getEmail(),
                                                courseName,
                                                scheduledDateTime
                                        );
                                        emailSent = true;
                                        emailMethod = "SMTP";
                                        logger.info("[{}] Email SENT via {} FALLBACK for schedule ID: {} -> {} ({} - {})",
                                                LocalDateTime.now(),
                                                emailMethod,
                                                scheduleId,
                                                emailDto.getEmail(),
                                                emailDto.getUserType(),
                                                emailDto.getUserName());
                                    } catch (Exception smtpException) {
                                        logger.error("Both SendGrid AND SMTP failed for {} in schedule {}: SendGrid={}, SMTP={}",
                                                emailDto.getEmail(), scheduleId, sendgridException.getMessage(), smtpException.getMessage());
                                    }
                                } else {
                                    logger.error("❌ SendGrid failed and SMTP fallback disabled for {} in schedule {}: {}",
                                            emailDto.getEmail(), scheduleId, sendgridException.getMessage());
                                }
                            }


                    } else if (useSmtpFallback) {
                            // Use SMTP directly if SendGrid not configured
                            try {
                                smtpEmailService.sendReminderEmail(emailDto.getEmail(), courseName, scheduledDateTime);
                                emailSent = true;
                                emailMethod = "SMTP";
                                logger.info("✅ [{}] Email SENT via {} for schedule ID: {} -> {} ({} - {})",
                                        LocalDateTime.now(),
                                        emailMethod,
                                        scheduleId,
                                        emailDto.getEmail(),
                                        emailDto.getUserType(),
                                        emailDto.getUserName());
                            } catch (Exception smtpException) {
                                logger.error("❌ SMTP failed for {} in schedule {}: {}",
                                        emailDto.getEmail(), scheduleId, smtpException.getMessage());
                            }
                        } else {
                            logger.warn("⚠️ No email service configured! Set SENDGRID_API_KEY or enable SMTP fallback");
                        }
                    } else {
                        logger.info("📧 [{}] Email QUEUED (not sent - notifications disabled) for schedule ID: {} -> {} ({} - {})",
                                LocalDateTime.now(),
                                scheduleId,
                                emailDto.getEmail(),
                                emailDto.getUserType(),
                                emailDto.getUserName());
                    }
                } catch (Exception parseException) {
                    logger.error("Error parsing date/time for schedule {}: date={}, time={}",
                            scheduleId, scheduledDate, scheduledTime, parseException);

                    // Fallback: just log without parsing
                    logger.info("[{}] Email fired for schedule ID: {} -> {} ({} - {})",
                            LocalDateTime.now(),
                            scheduleId,
                            emailDto.getEmail(),
                            emailDto.getUserType(),
                            emailDto.getUserName());
                }
            }

            logger.info("================================");
            if (emailNotificationsEnabled) {
                logger.info("🔔 {} email notifications processed! 🔔", emails.size());
                if (sendgridApiKey != null && !sendgridApiKey.trim().isEmpty()) {
                    logger.info("✉️ SendGrid configured and ready");
                }
                if (useSmtpFallback) {
                    logger.info("📧 SMTP fallback enabled");
                }
            } else {
                logger.info("📧 {} email notifications logged (not sent - notifications disabled)", emails.size());
                logger.info("💡 To enable email sending, set EMAIL_NOTIFICATIONS_ENABLED=true");
                logger.info("💡 Configure SENDGRID_API_KEY or set USE_SMTP_FALLBACK=true with SMTP credentials");
            }

        } catch (Exception e) {
            logger.error("Error sending notifications for schedule {}", scheduleId, e);
        }
    }

}