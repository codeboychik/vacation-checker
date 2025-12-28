package com.example.vacationchecker.tempo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TempoPlannedTime(
        TempoPlannedTimeMetadata metadata
) {
}
