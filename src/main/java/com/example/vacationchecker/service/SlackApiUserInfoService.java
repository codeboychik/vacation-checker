package com.example.vacationchecker.service;

import com.example.vacationchecker.config.SlackProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Service
public class SlackApiUserInfoService implements SlackUserInfoService {

    private static final String SLACK_API_BASE = "https://slack.com/api";

    private final SlackProperties slackProperties;
    private final RestClient restClient;

    public SlackApiUserInfoService(SlackProperties slackProperties, RestClient.Builder builder) {
        this.slackProperties = slackProperties;
        this.restClient = builder.baseUrl(SLACK_API_BASE).build();
    }

    @Override
    public Optional<String> resolveEmail(String userMention) {
        if (!StringUtils.hasText(slackProperties.botToken())) {
            return Optional.empty();
        }
        String userId = extractUserId(userMention);
        if (!StringUtils.hasText(userId)) {
            return Optional.empty();
        }
        SlackUserInfoResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/users.info")
                        .queryParam("user", userId)
                        .build())
                .headers(this::applyAuth)
                .retrieve()
                .body(SlackUserInfoResponse.class);
        if (response == null || !response.ok() || response.user() == null || response.user().profile() == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(response.user().profile().email())
                .filter(StringUtils::hasText);
    }

    private void applyAuth(HttpHeaders headers) {
        if (!StringUtils.hasText(slackProperties.botToken())) {
            return;
        }
        headers.setBearerAuth(slackProperties.botToken());
    }

    private String extractUserId(String mention) {
        if (!StringUtils.hasText(mention)) {
            return null;
        }
        String trimmed = mention.trim();
        if (trimmed.startsWith("<@")) {
            int start = 2;
            int end = trimmed.indexOf('|');
            if (end == -1) {
                end = trimmed.indexOf('>');
            }
            if (end <= start) {
                return null;
            }
            return trimmed.substring(start, end);
        }
        if (trimmed.startsWith("@")) {
            return trimmed.substring(1);
        }
        return trimmed;
    }

    private record SlackUserInfoResponse(
            boolean ok,
            SlackUser user
    ) {
    }

    private record SlackUser(
            SlackUserProfile profile
    ) {
    }

    private record SlackUserProfile(
            String email
    ) {
    }
}
