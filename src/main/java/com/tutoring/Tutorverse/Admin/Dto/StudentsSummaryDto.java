package com.tutoring.Tutorverse.Admin.Dto;

public class StudentsSummaryDto {
    public long activeStudents;
    public long inactiveStudents;

    public StudentsSummaryDto() {}

    public StudentsSummaryDto(long activeStudents, long inactiveStudents) {
        this.activeStudents = activeStudents;
        this.inactiveStudents = inactiveStudents;
    }
}
