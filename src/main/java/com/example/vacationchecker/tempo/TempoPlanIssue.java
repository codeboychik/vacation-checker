package com.example.vacationchecker.tempo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TempoPlanIssue(
        @JsonAlias({"key", "issueKey", "issue_key"})
        String key,
        @JsonAlias({"summary", "name"})
        String summary
) {
}
