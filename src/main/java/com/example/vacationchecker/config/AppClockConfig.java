package com.example.vacationchecker.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppClockConfig {

    private static final Logger log = LoggerFactory.getLogger(AppClockConfig.class);

    @Bean
    public Clock appClock(ApplicationProperties properties) {
        String tz = properties.timezone();
        try {
            return Clock.system(ZoneId.of(tz));
        } catch (Exception ex) {
            log.warn("Invalid APP_TIMEZONE '{}', falling back to UTC", tz);
            return Clock.systemUTC();
        }
    }
}
