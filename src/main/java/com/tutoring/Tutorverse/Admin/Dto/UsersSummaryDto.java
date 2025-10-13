package com.tutoring.Tutorverse.Admin.Dto;

public class UsersSummaryDto {
    public long totalUsers;
    public long admins;
    public long tutors;
    public long students;
    public long usersWith2FA;

    public UsersSummaryDto() {}

    public UsersSummaryDto(long totalUsers, long admins, long tutors, long students, long usersWith2FA) {
        this.totalUsers = totalUsers;
        this.admins = admins;
        this.tutors = tutors;
        this.students = students;
        this.usersWith2FA = usersWith2FA;
    }
}
