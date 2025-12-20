package com.example.vacationchecker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "capacity-planner")
public record CapacityPlannerProperties(
        String baseUrl,
        String token,
        String scheduleId
) {
}
