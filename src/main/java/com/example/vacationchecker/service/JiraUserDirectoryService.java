package com.example.vacationchecker.service;

import com.example.vacationchecker.config.JiraProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.Optional;

@Service
public class JiraUserDirectoryService {

    private final JiraProperties jiraProperties;
    private final RestClient restClient;

    public JiraUserDirectoryService(JiraProperties jiraProperties, RestClient.Builder builder) {
        this.jiraProperties = jiraProperties;
        RestClient.Builder clientBuilder = builder;
        if (StringUtils.hasText(jiraProperties.baseUrl())) {
            clientBuilder = clientBuilder.baseUrl(jiraProperties.baseUrl());
        }
        this.restClient = clientBuilder.build();
    }

    public Optional<String> findAccountIdByEmail(String email) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(jiraProperties.baseUrl())) {
            return Optional.empty();
        }
        JiraUser[] users = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/rest/api/3/user/search")
                        .queryParam("query", email)
                        .queryParam("maxResults", 1)
                        .build())
                .headers(this::applyAuth)
                .retrieve()
                .body(JiraUser[].class);
        if (users == null || users.length == 0) {
            return Optional.empty();
        }
        return Arrays.stream(users)
                .map(this::extractAccountId)
                .filter(StringUtils::hasText)
                .findFirst();
    }

    private String extractAccountId(JiraUser user) {
        if (user == null) {
            return null;
        }
        if (StringUtils.hasText(user.accountId())) {
            return user.accountId();
        }
        if (StringUtils.hasText(user.name())) {
            return user.name();
        }
        return user.key();
    }

    private void applyAuth(HttpHeaders headers) {
        if (!StringUtils.hasText(jiraProperties.username()) || !StringUtils.hasText(jiraProperties.apiToken())) {
            return;
        }
        headers.setBasicAuth(jiraProperties.username(), jiraProperties.apiToken());
    }

    private record JiraUser(
            String accountId,
            String name,
            String key,
            String emailAddress,
            String displayName
    ) {
    }
}
