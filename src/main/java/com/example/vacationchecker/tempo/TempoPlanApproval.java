package com.example.vacationchecker.tempo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TempoPlanApproval(
        String status,
        TempoPlanApprovalUser reviewer,
        TempoPlanApprovalUser actor,
        TempoPlanApprovalUser requester
) {
}
