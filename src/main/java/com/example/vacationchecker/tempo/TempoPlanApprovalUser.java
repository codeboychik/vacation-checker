package com.example.vacationchecker.tempo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TempoPlanApprovalUser(
        String accountId,
        String self
) {
}
