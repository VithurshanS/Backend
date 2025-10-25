package com.tutoring.Tutorverse.Admin.Dto;

import java.util.List;

public class RevenueSummaryDto {
    public double totalRevenue;
    public double revenueLast30Days;
    public List<AnalyticsOverviewDto.RevenueTrendPoint> last6Months;

    public RevenueSummaryDto() {}

    public RevenueSummaryDto(double totalRevenue, double revenueLast30Days, List<AnalyticsOverviewDto.RevenueTrendPoint> last6Months) {
        this.totalRevenue = totalRevenue;
        this.revenueLast30Days = revenueLast30Days;
        this.last6Months = last6Months;
    }
}
