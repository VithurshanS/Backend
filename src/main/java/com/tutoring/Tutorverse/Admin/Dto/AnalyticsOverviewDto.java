package com.tutoring.Tutorverse.Admin.Dto;

import java.util.List;

public class AnalyticsOverviewDto {
    public static class CountBreakdown {
        public long total;
        public long last30Days;
        public long last7Days;
        public CountBreakdown() {}
        public CountBreakdown(long total, long last30Days, long last7Days) {
            this.total = total;
            this.last30Days = last30Days;
            this.last7Days = last7Days;
        }
    }

    public static class TutorStatusCounts {
        public long approved;
        public long pending;
        public long banned;
        public TutorStatusCounts() {}
        public TutorStatusCounts(long approved, long pending, long banned) {
            this.approved = approved;
            this.pending = pending;
            this.banned = banned;
        }
    }

    public static class RevenueTrendPoint {
        public String month; // YYYY-MM
        public double amount;
        public RevenueTrendPoint() {}
        public RevenueTrendPoint(String month, double amount) {
            this.month = month;
            this.amount = amount;
        }
    }

    public static class TopItem {
        public String id; // UUID as string
        public String name; // module or tutor name
        public double value; // revenue or count
        public TopItem() {}
        public TopItem(String id, String name, double value) {
            this.id = id;
            this.name = name;
            this.value = value;
        }
    }

    // Users
    public CountBreakdown users;
    public long admins;
    public long tutors;
    public long students;
    public long usersWith2FA;

    // Students
    public long activeStudents;
    public long inactiveStudents;

    // Tutors
    public TutorStatusCounts tutorStatuses;

    // Modules
    public CountBreakdown modules;
    public long activeModules;

    // Enrollments
    public long enrollments;

    // Payments / Revenue
    public double totalRevenue;
    public double revenueLast30Days;
    public List<RevenueTrendPoint> revenueLast6Months;

    // Ratings
    public double averageRating;

    // Upcoming schedules
    public long upcomingSchedules;

    // Top modules by revenue
    public List<TopItem> topModulesByRevenue;

    public AnalyticsOverviewDto() {}
}
