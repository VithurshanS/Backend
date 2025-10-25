package com.tutoring.Tutorverse.Model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_tracking",
       uniqueConstraints = @UniqueConstraint(
           columnNames = {"schedule_id", "scheduled_date", "scheduled_time"}
       ))
public class NotificationTrackingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime;

    @Column(name = "notification_fired_at", nullable = false)
    private ZonedDateTime notificationFiredAt;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    // Constructors
    public NotificationTrackingEntity() {
        this.createdAt = ZonedDateTime.now();
    }

    public NotificationTrackingEntity(UUID scheduleId, LocalDate scheduledDate, LocalTime scheduledTime) {
        this();
        this.scheduleId = scheduleId;
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
        this.notificationFiredAt = ZonedDateTime.now();
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(UUID scheduleId) {
        this.scheduleId = scheduleId;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public ZonedDateTime getNotificationFiredAt() {
        return notificationFiredAt;
    }

    public void setNotificationFiredAt(ZonedDateTime notificationFiredAt) {
        this.notificationFiredAt = notificationFiredAt;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "NotificationTrackingEntity{" +
                "id=" + id +
                ", scheduleId=" + scheduleId +
                ", scheduledDate=" + scheduledDate +
                ", scheduledTime=" + scheduledTime +
                ", notificationFiredAt=" + notificationFiredAt +
                '}';
    }
}