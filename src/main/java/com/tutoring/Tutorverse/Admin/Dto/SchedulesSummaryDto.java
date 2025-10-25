package com.tutoring.Tutorverse.Admin.Dto;

public class SchedulesSummaryDto {
    public long upcomingSchedules;
    public long enrollments;

    public SchedulesSummaryDto() {}

    public SchedulesSummaryDto(long upcomingSchedules, long enrollments) {
        this.upcomingSchedules = upcomingSchedules;
        this.enrollments = enrollments;
    }
}
