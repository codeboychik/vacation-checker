package com.example.vacationchecker.tempo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TempoPlanItem(
        String id,
        String type,
        String self
) {
}
