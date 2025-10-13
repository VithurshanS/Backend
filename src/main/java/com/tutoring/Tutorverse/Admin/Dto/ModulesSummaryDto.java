package com.tutoring.Tutorverse.Admin.Dto;

public class ModulesSummaryDto {
    public long total;
    public long active;
    public long last30Days;
    public long last7Days;

    public ModulesSummaryDto() {}

    public ModulesSummaryDto(long total, long active, long last30Days, long last7Days) {
        this.total = total;
        this.active = active;
        this.last30Days = last30Days;
        this.last7Days = last7Days;
    }
}
