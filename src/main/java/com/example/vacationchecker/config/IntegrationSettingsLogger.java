package com.example.vacationchecker.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class IntegrationSettingsLogger {

    private static final Logger log = LoggerFactory.getLogger(IntegrationSettingsLogger.class);

    private final SlackProperties slackProperties;
    private final JiraProperties jiraProperties;
    private final CapacityPlannerProperties capacityPlannerProperties;
    private final ApplicationProperties applicationProperties;

    public IntegrationSettingsLogger(SlackProperties slackProperties,
                                     JiraProperties jiraProperties,
                                     CapacityPlannerProperties capacityPlannerProperties,
                                     ApplicationProperties applicationProperties) {
        this.slackProperties = slackProperties;
        this.jiraProperties = jiraProperties;
        this.capacityPlannerProperties = capacityPlannerProperties;
        this.applicationProperties = applicationProperties;
    }

    @PostConstruct
    public void logConfiguredIntegrations() {
        log.info("Configured Slack bot token: {}", mask(slackProperties.botToken()));
        log.info("Configured Slack signing secret: {}", mask(slackProperties.signingSecret()));
        log.info("Configured Jira base URL: {}", defaultIfBlank(jiraProperties.baseUrl(), "<not set>"));
        log.info("Configured Jira projects: {}", defaultIfBlank(jiraProperties.projectKeys(), "<not set>"));
        log.info("Configured Capacity Planner URL: {}",
                defaultIfBlank(capacityPlannerProperties.baseUrl(), "<not set>"));
        log.info("Configured Capacity Planner plans path: {}",
                defaultIfBlank(capacityPlannerProperties.plansPath(), "<not set>"));
        log.info("Configured Capacity Planner schedule ID: {}",
                defaultIfBlank(capacityPlannerProperties.scheduleId(), "<not set>"));
        log.info("Configured Capacity Planner approved statuses: {}",
                defaultIfBlank(join(capacityPlannerProperties.approvedStatuses()), "<not set>"));
        log.info("Configured Capacity Planner time-off types: {}",
                defaultIfBlank(join(capacityPlannerProperties.timeOffTypes()), "<not set>"));
        log.info("Application timezone: {}", applicationProperties.timezone());
    }

    private String mask(String value) {
        if (value == null || value.isBlank()) {
            return "<not set>";
        }
        return value.length() <= 4 ? "****" : "****" + value.substring(value.length() - 4);
    }

    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private String join(Iterable<String> values) {
        if (values == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(", ");
            }
            builder.append(value);
        }
        return builder.isEmpty() ? null : builder.toString();
    }
}
