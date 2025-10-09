package com.tutoring.Tutorverse.Repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

import com.tutoring.Tutorverse.Model.NotificationTrackingEntity;
import com.tutoring.Tutorverse.TestUtils.BaseRepositoryTest;

import jakarta.transaction.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
@Rollback(true)
public class NotificationTrackingRepoTest extends BaseRepositoryTest {

    @Autowired
    private NotificationTrackingRepository notificationTrackingRepository;

    private UUID testScheduleId;
    private LocalDate testDate;
    private LocalTime testTime;

    @BeforeEach
    public void setUp() {
        testScheduleId = UUID.randomUUID();
        testDate = LocalDate.now().plusDays(1);
        testTime = LocalTime.of(10, 0);
    }

    @Test
    public void testCreateNotificationTracking() {
        NotificationTrackingEntity notification = new NotificationTrackingEntity(
            testScheduleId, 
            testDate, 
            testTime
        );

        NotificationTrackingEntity savedNotification = notificationTrackingRepository.save(notification);
        entityManager.flush();

        assertThat(savedNotification.getId()).isNotNull();
        assertThat(savedNotification.getScheduleId()).isEqualTo(testScheduleId);
        assertThat(savedNotification.getScheduledDate()).isEqualTo(testDate);
        assertThat(savedNotification.getScheduledTime()).isEqualTo(testTime);
        assertThat(savedNotification.getNotificationFiredAt()).isNotNull();
        assertThat(savedNotification.getCreatedAt()).isNotNull();
    }

    @Test
    public void testCreateNotificationWithDefaultConstructor() {
        NotificationTrackingEntity notification = new NotificationTrackingEntity();
        notification.setScheduleId(testScheduleId);
        notification.setScheduledDate(testDate);
        notification.setScheduledTime(testTime);
        notification.setNotificationFiredAt(ZonedDateTime.now());

        NotificationTrackingEntity savedNotification = notificationTrackingRepository.save(notification);
        entityManager.flush();

        assertThat(savedNotification.getId()).isNotNull();
        assertThat(savedNotification.getCreatedAt()).isNotNull();
    }

    @Test
    public void testNotificationWithMissingMandatoryFields() {
        // Test missing scheduleId
        NotificationTrackingEntity notificationWithoutScheduleId = new NotificationTrackingEntity();
        notificationWithoutScheduleId.setScheduledDate(testDate);
        notificationWithoutScheduleId.setScheduledTime(testTime);
        notificationWithoutScheduleId.setNotificationFiredAt(ZonedDateTime.now());

        assertThatThrownBy(() -> {
            notificationTrackingRepository.save(notificationWithoutScheduleId);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);

        // Test missing scheduledDate
        NotificationTrackingEntity notificationWithoutDate = new NotificationTrackingEntity();
        notificationWithoutDate.setScheduleId(testScheduleId);
        notificationWithoutDate.setScheduledTime(testTime);
        notificationWithoutDate.setNotificationFiredAt(ZonedDateTime.now());

        assertThatThrownBy(() -> {
            notificationTrackingRepository.save(notificationWithoutDate);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);

        // Test missing scheduledTime
        NotificationTrackingEntity notificationWithoutTime = new NotificationTrackingEntity();
        notificationWithoutTime.setScheduleId(testScheduleId);
        notificationWithoutTime.setScheduledDate(testDate);
        notificationWithoutTime.setNotificationFiredAt(ZonedDateTime.now());

        assertThatThrownBy(() -> {
            notificationTrackingRepository.save(notificationWithoutTime);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);

        // Test missing notificationFiredAt
        NotificationTrackingEntity notificationWithoutFiredAt = new NotificationTrackingEntity();
        notificationWithoutFiredAt.setScheduleId(testScheduleId);
        notificationWithoutFiredAt.setScheduledDate(testDate);
        notificationWithoutFiredAt.setScheduledTime(testTime);

        assertThatThrownBy(() -> {
            notificationTrackingRepository.save(notificationWithoutFiredAt);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testExistsByScheduleIdAndScheduledDateAndScheduledTime() {
        NotificationTrackingEntity notification = new NotificationTrackingEntity(
            testScheduleId, 
            testDate, 
            testTime
        );

        notificationTrackingRepository.save(notification);
        entityManager.flush();

        boolean exists = notificationTrackingRepository.existsByScheduleIdAndScheduledDateAndScheduledTime(
            testScheduleId, testDate, testTime
        );
        assertThat(exists).isTrue();

        // Test with different parameters
        boolean notExists1 = notificationTrackingRepository.existsByScheduleIdAndScheduledDateAndScheduledTime(
            UUID.randomUUID(), testDate, testTime
        );
        assertThat(notExists1).isFalse();

        boolean notExists2 = notificationTrackingRepository.existsByScheduleIdAndScheduledDateAndScheduledTime(
            testScheduleId, testDate.plusDays(1), testTime
        );
        assertThat(notExists2).isFalse();

        boolean notExists3 = notificationTrackingRepository.existsByScheduleIdAndScheduledDateAndScheduledTime(
            testScheduleId, testDate, testTime.plusHours(1)
        );
        assertThat(notExists3).isFalse();
    }

    @Test
    public void testUniqueConstraint() {
        NotificationTrackingEntity notification1 = new NotificationTrackingEntity(
            testScheduleId, 
            testDate, 
            testTime
        );

        notificationTrackingRepository.save(notification1);
        entityManager.flush();

        // Try to create another notification with the same schedule, date, and time
        NotificationTrackingEntity notification2 = new NotificationTrackingEntity(
            testScheduleId, 
            testDate, 
            testTime
        );

        // This should fail due to unique constraint
        assertThatThrownBy(() -> {
            notificationTrackingRepository.save(notification2);
            entityManager.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void testDeleteOldNotifications() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime oldTime = now.minusDays(30);
        ZonedDateTime recentTime = now.minusHours(1);

        // Create old notification
        NotificationTrackingEntity oldNotification = new NotificationTrackingEntity();
        oldNotification.setScheduleId(UUID.randomUUID());
        oldNotification.setScheduledDate(testDate);
        oldNotification.setScheduledTime(testTime);
        oldNotification.setNotificationFiredAt(oldTime);

        // Create recent notification
        NotificationTrackingEntity recentNotification = new NotificationTrackingEntity();
        recentNotification.setScheduleId(UUID.randomUUID());
        recentNotification.setScheduledDate(testDate.plusDays(1));
        recentNotification.setScheduledTime(testTime.plusHours(1));
        recentNotification.setNotificationFiredAt(recentTime);

        notificationTrackingRepository.save(oldNotification);
        notificationTrackingRepository.save(recentNotification);
        entityManager.flush();

        long initialCount = notificationTrackingRepository.count();
        assertThat(initialCount).isGreaterThanOrEqualTo(2);

        // Delete notifications older than 7 days
        ZonedDateTime cutoffDate = now.minusDays(7);
        int deletedCount = notificationTrackingRepository.deleteOldNotifications(cutoffDate);
        entityManager.flush();

        assertThat(deletedCount).isGreaterThanOrEqualTo(1);

        long finalCount = notificationTrackingRepository.count();
        assertThat(finalCount).isLessThan(initialCount);

        // Recent notification should still exist
        boolean recentExists = notificationTrackingRepository.existsByScheduleIdAndScheduledDateAndScheduledTime(
            recentNotification.getScheduleId(),
            recentNotification.getScheduledDate(),
            recentNotification.getScheduledTime()
        );
        assertThat(recentExists).isTrue();
    }

    @Test
    public void testCountTotalNotifications() {
        long initialCount = notificationTrackingRepository.countTotalNotifications();

        NotificationTrackingEntity notification1 = new NotificationTrackingEntity(
            UUID.randomUUID(), testDate, testTime
        );
        NotificationTrackingEntity notification2 = new NotificationTrackingEntity(
            UUID.randomUUID(), testDate.plusDays(1), testTime.plusHours(1)
        );

        notificationTrackingRepository.save(notification1);
        notificationTrackingRepository.save(notification2);
        entityManager.flush();

        long newCount = notificationTrackingRepository.countTotalNotifications();
        assertThat(newCount).isEqualTo(initialCount + 2);
    }

    @Test
    public void testCountTodayNotifications() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime startOfDay = now.toLocalDate().atStartOfDay(now.getZone());
        ZonedDateTime startOfNextDay = startOfDay.plusDays(1);

        // Create notification from today
        NotificationTrackingEntity todayNotification = new NotificationTrackingEntity();
        todayNotification.setScheduleId(UUID.randomUUID());
        todayNotification.setScheduledDate(testDate);
        todayNotification.setScheduledTime(testTime);
        todayNotification.setNotificationFiredAt(now);

        // Create notification from yesterday
        NotificationTrackingEntity yesterdayNotification = new NotificationTrackingEntity();
        yesterdayNotification.setScheduleId(UUID.randomUUID());
        yesterdayNotification.setScheduledDate(testDate.plusDays(1));
        yesterdayNotification.setScheduledTime(testTime.plusHours(1));
        yesterdayNotification.setNotificationFiredAt(now.minusDays(1));

        notificationTrackingRepository.save(todayNotification);
        notificationTrackingRepository.save(yesterdayNotification);
        entityManager.flush();

        long todayCount = notificationTrackingRepository.countTodayNotifications(startOfDay, startOfNextDay);
        assertThat(todayCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    public void testFindByScheduleId() {
        UUID scheduleId = UUID.randomUUID();

        NotificationTrackingEntity notification1 = new NotificationTrackingEntity(
            scheduleId, testDate, testTime
        );
        NotificationTrackingEntity notification2 = new NotificationTrackingEntity(
            scheduleId, testDate.plusDays(1), testTime.plusHours(1)
        );
        NotificationTrackingEntity notification3 = new NotificationTrackingEntity(
            UUID.randomUUID(), testDate, testTime.plusHours(2)
        );

        notificationTrackingRepository.save(notification1);
        notificationTrackingRepository.save(notification2);
        notificationTrackingRepository.save(notification3);
        entityManager.flush();

        List<NotificationTrackingEntity> scheduleNotifications = 
            notificationTrackingRepository.findByScheduleId(scheduleId);

        assertThat(scheduleNotifications).hasSize(2);
        assertThat(scheduleNotifications).allMatch(n -> n.getScheduleId().equals(scheduleId));
        
        // Should be ordered by notificationFiredAt DESC
        if (scheduleNotifications.size() > 1) {
            ZonedDateTime first = scheduleNotifications.get(0).getNotificationFiredAt();
            ZonedDateTime second = scheduleNotifications.get(1).getNotificationFiredAt();
            assertThat(first).isAfterOrEqualTo(second);
        }
    }

    @Test
    public void testMultipleNotificationsForDifferentSchedules() {
        UUID schedule1Id = UUID.randomUUID();
        UUID schedule2Id = UUID.randomUUID();

        NotificationTrackingEntity notification1 = new NotificationTrackingEntity(
            schedule1Id, testDate, testTime
        );
        NotificationTrackingEntity notification2 = new NotificationTrackingEntity(
            schedule2Id, testDate, testTime
        );

        notificationTrackingRepository.save(notification1);
        notificationTrackingRepository.save(notification2);
        entityManager.flush();

        // Both should exist since they have different schedule IDs
        boolean exists1 = notificationTrackingRepository.existsByScheduleIdAndScheduledDateAndScheduledTime(
            schedule1Id, testDate, testTime
        );
        boolean exists2 = notificationTrackingRepository.existsByScheduleIdAndScheduledDateAndScheduledTime(
            schedule2Id, testDate, testTime
        );

        assertThat(exists1).isTrue();
        assertThat(exists2).isTrue();
    }

    @Test
    public void testNotificationTrackingToString() {
        NotificationTrackingEntity notification = new NotificationTrackingEntity(
            testScheduleId, testDate, testTime
        );

        NotificationTrackingEntity savedNotification = notificationTrackingRepository.save(notification);
        entityManager.flush();

        String toStringResult = savedNotification.toString();
        assertThat(toStringResult).contains("NotificationTrackingEntity");
        assertThat(toStringResult).contains(testScheduleId.toString());
        assertThat(toStringResult).contains(testDate.toString());
        assertThat(toStringResult).contains(testTime.toString());
    }

    @Test
    public void testUpdateNotification() {
        NotificationTrackingEntity notification = new NotificationTrackingEntity(
            testScheduleId, testDate, testTime
        );

        NotificationTrackingEntity savedNotification = notificationTrackingRepository.save(notification);
        entityManager.flush();

        // Update notification fired at time
        ZonedDateTime newFiredAt = ZonedDateTime.now().plusHours(1);
        savedNotification.setNotificationFiredAt(newFiredAt);

        NotificationTrackingEntity updatedNotification = notificationTrackingRepository.save(savedNotification);
        entityManager.flush();

        assertThat(updatedNotification.getNotificationFiredAt()).isEqualTo(newFiredAt);
    }

    @Test
    public void testDeleteNotification() {
        NotificationTrackingEntity notification = new NotificationTrackingEntity(
            testScheduleId, testDate, testTime
        );

        NotificationTrackingEntity savedNotification = notificationTrackingRepository.save(notification);
        entityManager.flush();

        UUID notificationId = savedNotification.getId();
        
        notificationTrackingRepository.delete(savedNotification);
        entityManager.flush();

        assertThat(notificationTrackingRepository.findById(notificationId)).isEmpty();
        
        boolean exists = notificationTrackingRepository.existsByScheduleIdAndScheduledDateAndScheduledTime(
            testScheduleId, testDate, testTime
        );
        assertThat(exists).isFalse();
    }
}