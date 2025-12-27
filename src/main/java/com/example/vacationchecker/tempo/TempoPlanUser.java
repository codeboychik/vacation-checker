package com.example.vacationchecker.tempo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TempoPlanUser(
        @JsonAlias({"accountId", "account_id", "id"})
        String accountId,
        @JsonAlias({"userKey", "user_key"})
        String userKey,
        @JsonAlias({"displayName", "display_name"})
        String displayName,
        @JsonAlias({"email", "emailAddress"})
        String email
) {
}
