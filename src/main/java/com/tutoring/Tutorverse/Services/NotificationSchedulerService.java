package com.tutoring.Tutorverse.Services;

import com.tutoring.Tutorverse.Model.NotificationTrackingEntity;
import com.tutoring.Tutorverse.Repository.NotificationTrackingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSchedulerService.class);
    
    // Sri Lanka timezone (GMT+5:30)
    private static final ZoneId SRI_LANKA_TIMEZONE = ZoneId.of("Asia/Colombo");

    @Autowired
    private ScheduleService scheduleService;
    
    @Autowired
    private EmailNotificationService emailNotificationService;
    
    @Autowired
    private NotificationTrackingRepository notificationTrackingRepository;
    
    // Constructor to log timezone info
    public NotificationSchedulerService() {
        logger.info("🌍 NotificationSchedulerService initialized with Sri Lanka timezone: {} (GMT+5:30)", SRI_LANKA_TIMEZONE);
        logger.info("🕐 Current Sri Lanka time: {}", ZonedDateTime.now(SRI_LANKA_TIMEZONE));
    }

    /**
     * Runs every 5 minutes to check schedules and send notifications
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5 minutes in milliseconds
    public void scheduledNotificationCheck() {
        ZonedDateTime currentTime = ZonedDateTime.now(SRI_LANKA_TIMEZONE);
        logger.info("Starting scheduled notification check at {} (Sri Lanka Time)", currentTime);
        checkSchedulesAndNotify();
    }

    /**
     * Main logic function that checks schedules and triggers notifications
     */
    public void checkSchedulesAndNotify() {
        try {
            // Get current time in Sri Lanka timezone (GMT+5:30)
            ZonedDateTime currentZonedDateTime = ZonedDateTime.now(SRI_LANKA_TIMEZONE);
            LocalDateTime currentDateTime = currentZonedDateTime.toLocalDateTime();

            logger.info("Checking schedules for notifications at {} (Sri Lanka Time GMT+5:30)", currentDateTime);

            // Call existing function to get upcoming schedules
            List<ScheduleService.UpcomingSessionResponse> upcomingSchedules = getUpcomingModules(currentDateTime);

            logger.info("Found {} schedules to check", upcomingSchedules.size());

            int notificationsFired = 0;

            for (ScheduleService.UpcomingSessionResponse schedule : upcomingSchedules) {
                UUID scheduleId = schedule.schedule_id;

                // Check if schedule is active
                if (Boolean.TRUE.equals(schedule.active)) {
                    
                    // Convert string date and time to proper types for database check
                    LocalDate scheduleDate = LocalDate.parse(schedule.Date);
                    LocalTime scheduleTime = LocalTime.parse(schedule.time);
                    
                    // Check if we've already fired a notification for this specific schedule + date + time combination
                    boolean alreadyNotified = notificationTrackingRepository.existsByScheduleIdAndScheduledDateAndScheduledTime(
                        scheduleId, scheduleDate, scheduleTime);
                    
                    if (!alreadyNotified) {
                        
                        // Fire the email notification with Sri Lanka timezone
                        fireEmailNotification(schedule, currentZonedDateTime);
                        
                        // Record this notification in the database
                        recordNotification(scheduleId, scheduleDate, scheduleTime);
                        notificationsFired++;
                        
                        logger.info("Email notification fired for schedule ID: {} (Course: {}, Tutor: {}, Date: {}, Time: {}) - Sri Lanka Time (GMT+5:30)", 
                                   scheduleId, schedule.course, schedule.tutor, schedule.Date, schedule.time);
                    } else {
                        logger.debug("Schedule ID {} for date {} and time {} already notified, skipping", 
                                   scheduleId, schedule.Date, schedule.time);
                    }
                } else {
                    logger.debug("Schedule ID {} is not active, skipping", scheduleId);
                }
            }

            logger.info("Notification check completed. Fired {} new notifications", notificationsFired);

        } catch (Exception e) {
            logger.error("Error during scheduled notification check", e);
        }
    }

    /**
     * Wrapper function that calls the existing getUpcomingSchedules methods
     * This mimics the getUpcomingModules function you mentioned
     */
    private List<ScheduleService.UpcomingSessionResponse> getUpcomingModules(LocalDateTime currentDateTime) {
        LocalDate currentDate = currentDateTime.toLocalDate();
        LocalTime currentTime = currentDateTime.toLocalTime();
        
        // Use the existing method to get upcoming schedules for all tutors
        // This calls the database function that handles daily/weekly/specific logic
        // Pass null for tutorId to get schedules for all tutors
        return scheduleService.getUpcomingSessionsByTutor(currentDate, currentTime, null);
    }

    /**
     * Fires an email notification for a schedule
     * Uses EmailNotificationService to get all emails and send notifications
     */
    private void fireEmailNotification(ScheduleService.UpcomingSessionResponse schedule, ZonedDateTime timestamp) {
        try {
            // Use the new EmailNotificationService to send notifications to all users (tutor + students)
            emailNotificationService.sendScheduleNotification(
                schedule.schedule_id,
                schedule.module_id,
                schedule.course,
                schedule.time,
                schedule.Date
            );

        } catch (Exception e) {
            logger.error("Error firing email notification for schedule {}", schedule.schedule_id, e);
        }
    }

    /**
     * Records a notification in the database to prevent duplicate notifications
     */
    private void recordNotification(UUID scheduleId, LocalDate scheduledDate, LocalTime scheduledTime) {
        try {
            NotificationTrackingEntity tracking = new NotificationTrackingEntity(scheduleId, scheduledDate, scheduledTime);
            notificationTrackingRepository.save(tracking);
            logger.debug("Recorded notification tracking for schedule {} on {} at {}", scheduleId, scheduledDate, scheduledTime);
        } catch (Exception e) {
            logger.error("Error recording notification for schedule {} on {} at {}", scheduleId, scheduledDate, scheduledTime, e);
        }
    }

    /**
     * Manual trigger method for testing
     */
    public void manualTrigger() {
        logger.info("Manual notification check triggered");
        checkSchedulesAndNotify();
    }

    /**
     * Method to reset fired notifications (useful for testing)
     * Clears all notification tracking records for today
     */
    public void resetFiredNotifications() {
        try {
            ZonedDateTime todayStart = ZonedDateTime.now(SRI_LANKA_TIMEZONE).withHour(0).withMinute(0).withSecond(0).withNano(0);
            int deletedCount = notificationTrackingRepository.deleteOldNotifications(todayStart);
            logger.info("Reset fired notifications. Deleted {} notification tracking records from today", deletedCount);
        } catch (Exception e) {
            logger.error("Error resetting fired notifications", e);
        }
    }

    /**
     * Get statistics about fired notifications for today
     */
    public int getFiredNotificationsCount() {
        try {
            ZonedDateTime now = ZonedDateTime.now(SRI_LANKA_TIMEZONE);
            ZonedDateTime startOfDay = now.toLocalDate().atStartOfDay(SRI_LANKA_TIMEZONE);
            ZonedDateTime startOfNextDay = startOfDay.plusDays(1);
            
            return (int) notificationTrackingRepository.countTodayNotifications(startOfDay, startOfNextDay);
        } catch (Exception e) {
            logger.error("Error getting fired notifications count", e);
            return 0;
        }
    }

    /**
     * Scheduled job to clean up old notification tracking records
     * Runs once daily at 2 AM Sri Lanka time
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "Asia/Colombo")
    public void cleanupOldNotificationRecords() {
        try {
            ZonedDateTime cutoffDate = ZonedDateTime.now(SRI_LANKA_TIMEZONE).minusDays(7); // Keep records for 7 days
            int deletedCount = notificationTrackingRepository.deleteOldNotifications(cutoffDate);
            logger.info("Daily cleanup: Deleted {} old notification tracking records older than {}", deletedCount, cutoffDate);
        } catch (Exception e) {
            logger.error("Error during daily cleanup of notification tracking records", e);
        }
    }
}