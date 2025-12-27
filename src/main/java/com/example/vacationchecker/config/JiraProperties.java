package com.example.vacationchecker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jira")
public record JiraProperties(
        String baseUrl,
        String username,
        String apiToken,
        String projectKeys,
        String startField,
        String endField,
        String reviewStatusField
) {
}
