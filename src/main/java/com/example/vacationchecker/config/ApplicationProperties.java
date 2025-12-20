package com.example.vacationchecker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(String timezone) {

    public String timezone() {
        return timezone == null || timezone.isBlank() ? "UTC" : timezone;
    }
}
