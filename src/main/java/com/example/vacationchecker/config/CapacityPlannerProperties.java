package com.example.vacationchecker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "capacity-planner")
public record CapacityPlannerProperties(
        String baseUrl,
        String token,
        String scheduleId,
        String plansPath,
        List<String> approvedStatuses,
        List<String> timeOffTypes
) {
}
