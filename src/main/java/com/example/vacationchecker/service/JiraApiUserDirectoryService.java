package com.example.vacationchecker.service;

import com.example.vacationchecker.config.JiraProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Service
public class JiraApiUserDirectoryService implements JiraUserDirectoryService {

    private final JiraProperties jiraProperties;
    private final RestClient restClient;

    public JiraApiUserDirectoryService(JiraProperties jiraProperties, RestClient.Builder builder) {
        this.jiraProperties = jiraProperties;
        RestClient.Builder clientBuilder = builder;
        if (StringUtils.hasText(jiraProperties.baseUrl())) {
            clientBuilder = clientBuilder.baseUrl(jiraProperties.baseUrl());
        }
        this.restClient = clientBuilder.build();
    }

    @Override
    public Optional<JiraUserProfile> resolveUserByEmail(String email) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(jiraProperties.baseUrl())) {
            return Optional.empty();
        }
        JiraUser[] response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/rest/api/2/user/search")
                        .queryParam("query", email)
                        .build())
                .headers(this::applyAuth)
                .retrieve()
                .body(JiraUser[].class);
        if (response == null || response.length == 0) {
            return Optional.empty();
        }
        List<JiraUser> users = List.of(response);
        JiraUser selected = users.stream()
                .filter(user -> Boolean.TRUE.equals(user.active()))
                .findFirst()
                .orElse(users.get(0));
        if (selected == null || !StringUtils.hasText(selected.accountId())) {
            return Optional.empty();
        }
        return Optional.of(new JiraUserProfile(selected.accountId(), selected.displayName()));
    }

    private void applyAuth(HttpHeaders headers) {
        if (!StringUtils.hasText(jiraProperties.username()) || !StringUtils.hasText(jiraProperties.apiToken())) {
            return;
        }
        headers.setBasicAuth(jiraProperties.username(), jiraProperties.apiToken());
    }

    private record JiraUser(
            String accountId,
            String emailAddress,
            String displayName,
            Boolean active
    ) {
    }
}
