package com.tutoring.Tutorverse.Admin.Dto;

import java.util.List;

public class TopModulesDto {
    public List<AnalyticsOverviewDto.TopItem> items;

    public TopModulesDto() {}

    public TopModulesDto(List<AnalyticsOverviewDto.TopItem> items) {
        this.items = items;
    }
}
