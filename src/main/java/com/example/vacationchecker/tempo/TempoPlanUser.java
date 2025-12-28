package com.example.vacationchecker.tempo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TempoPlanUser(
        @JsonAlias({"id", "accountId", "account_id"})
        String accountId,
        String type,
        String self
) {
}
