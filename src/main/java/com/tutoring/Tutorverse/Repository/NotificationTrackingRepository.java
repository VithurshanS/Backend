package com.tutoring.Tutorverse.Repository;

import com.tutoring.Tutorverse.Model.NotificationTrackingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Repository
public interface NotificationTrackingRepository extends JpaRepository<NotificationTrackingEntity, UUID> {

    /**
     * Check if a notification has already been fired for a specific schedule, date, and time
     */
    @Query("SELECT COUNT(nt) > 0 FROM NotificationTrackingEntity nt " +
           "WHERE nt.scheduleId = :scheduleId " +
           "AND nt.scheduledDate = :scheduledDate " +
           "AND nt.scheduledTime = :scheduledTime")
    boolean existsByScheduleIdAndScheduledDateAndScheduledTime(
            @Param("scheduleId") UUID scheduleId,
            @Param("scheduledDate") LocalDate scheduledDate,
            @Param("scheduledTime") LocalTime scheduledTime);

    /**
     * Delete old notification records (older than specified date)
     * This helps keep the table clean and prevents unlimited growth
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM NotificationTrackingEntity nt WHERE nt.notificationFiredAt < :cutoffDate")
    int deleteOldNotifications(@Param("cutoffDate") ZonedDateTime cutoffDate);

    /**
     * Count total notifications fired
     */
    @Query("SELECT COUNT(nt) FROM NotificationTrackingEntity nt")
    long countTotalNotifications();

    /**
     * Count notifications fired today
     */
    @Query("SELECT COUNT(nt) FROM NotificationTrackingEntity nt " +
           "WHERE nt.notificationFiredAt >= :startOfDay " +
           "AND nt.notificationFiredAt < :startOfNextDay")
    long countTodayNotifications(@Param("startOfDay") ZonedDateTime startOfDay, 
                                @Param("startOfNextDay") ZonedDateTime startOfNextDay);

    /**
     * Get notifications for a specific schedule ID (for debugging)
     */
    @Query("SELECT nt FROM NotificationTrackingEntity nt " +
           "WHERE nt.scheduleId = :scheduleId " +
           "ORDER BY nt.notificationFiredAt DESC")
    java.util.List<NotificationTrackingEntity> findByScheduleId(@Param("scheduleId") UUID scheduleId);
}