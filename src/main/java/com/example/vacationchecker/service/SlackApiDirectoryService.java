package com.example.vacationchecker.service;

import com.example.vacationchecker.config.SlackProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Service
public class SlackApiDirectoryService implements SlackDirectoryService {

    private static final String SLACK_API_BASE = "https://slack.com/api";
    private static final String USERGROUP_PREFIX = "<!subteam^";
    private static final String CHANNEL_PREFIX = "<#";

    private final SlackProperties slackProperties;
    private final RestClient restClient;

    public SlackApiDirectoryService(SlackProperties slackProperties, RestClient.Builder builder) {
        this.slackProperties = slackProperties;
        this.restClient = builder.baseUrl(SLACK_API_BASE).build();
    }

    @Override
    public boolean isGroup(String mention) {
        if (!StringUtils.hasText(mention)) {
            return false;
        }
        if (mention.startsWith(USERGROUP_PREFIX)) {
            return true;
        }
        String handle = stripPrefix(mention, "@");
        if (!StringUtils.hasText(handle)) {
            return false;
        }
        return resolveUserGroupByHandle(handle).isPresent();
    }

    @Override
    public boolean isChannel(String mention) {
        return StringUtils.hasText(mention) && (mention.startsWith("#") || mention.startsWith(CHANNEL_PREFIX));
    }

    @Override
    public List<String> resolveMembers(String mention) {
        if (!StringUtils.hasText(mention) || !StringUtils.hasText(slackProperties.botToken())) {
            return List.of();
        }
        if (isGroup(mention)) {
            return resolveGroupMembers(mention);
        }
        if (isChannel(mention)) {
            return resolveChannelMembers(mention);
        }
        return List.of();
    }

    private List<String> resolveGroupMembers(String mention) {
        String groupId = extractUserGroupId(mention);
        if (!StringUtils.hasText(groupId)) {
            String handle = stripPrefix(mention, "@");
            groupId = resolveUserGroupByHandle(handle).map(SlackUserGroup::id).orElse(null);
        }
        if (!StringUtils.hasText(groupId)) {
            return List.of();
        }
        String resolvedGroupId = groupId;
        SlackUserGroupUsersResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/usergroups.users.list")
                        .queryParam("usergroup", resolvedGroupId)
                        .build())
                .headers(this::applyAuth)
                .retrieve()
                .body(SlackUserGroupUsersResponse.class);
        if (response == null || !response.ok() || response.users() == null) {
            return List.of();
        }
        return response.users().stream()
                .map(this::asUserMention)
                .toList();
    }

    private Optional<SlackUserGroup> resolveUserGroupByHandle(String handle) {
        if (!StringUtils.hasText(handle)) {
            return Optional.empty();
        }
        SlackUserGroupListResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/usergroups.list").build())
                .headers(this::applyAuth)
                .retrieve()
                .body(SlackUserGroupListResponse.class);
        if (response == null || !response.ok() || response.usergroups() == null) {
            return Optional.empty();
        }
        return response.usergroups().stream()
                .filter(group -> handle.equalsIgnoreCase(group.handle()) || handle.equalsIgnoreCase(group.name()))
                .findFirst();
    }

    private List<String> resolveChannelMembers(String mention) {
        String channelId = extractChannelId(mention);
        if (!StringUtils.hasText(channelId)) {
            String name = stripPrefix(mention, "#");
            channelId = resolveChannelIdByName(name);
        }
        if (!StringUtils.hasText(channelId)) {
            return List.of();
        }
        String resolvedChannelId = channelId;
        SlackConversationMembersResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/conversations.members")
                        .queryParam("channel", resolvedChannelId)
                        .build())
                .headers(this::applyAuth)
                .retrieve()
                .body(SlackConversationMembersResponse.class);
        if (response == null || !response.ok() || response.members() == null) {
            return List.of();
        }
        return response.members().stream()
                .map(this::asUserMention)
                .toList();
    }

    private String resolveChannelIdByName(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        SlackConversationListResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/conversations.list")
                        .queryParam("exclude_archived", true)
                        .queryParam("limit", 1000)
                        .build())
                .headers(this::applyAuth)
                .retrieve()
                .body(SlackConversationListResponse.class);
        if (response == null || !response.ok() || response.channels() == null) {
            return null;
        }
        return response.channels().stream()
                .filter(channel -> name.equalsIgnoreCase(channel.name()))
                .map(SlackConversation::id)
                .findFirst()
                .orElse(null);
    }

    private void applyAuth(HttpHeaders headers) {
        if (!StringUtils.hasText(slackProperties.botToken())) {
            return;
        }
        headers.setBearerAuth(slackProperties.botToken());
    }

    private String extractUserGroupId(String mention) {
        if (!StringUtils.hasText(mention) || !mention.startsWith(USERGROUP_PREFIX)) {
            return null;
        }
        int start = mention.indexOf('^') + 1;
        int end = mention.indexOf('|');
        if (end == -1) {
            end = mention.indexOf('>');
        }
        if (start <= 0 || end <= start) {
            return null;
        }
        return mention.substring(start, end);
    }

    private String extractChannelId(String mention) {
        if (!StringUtils.hasText(mention) || !mention.startsWith(CHANNEL_PREFIX)) {
            return null;
        }
        int start = 2;
        int end = mention.indexOf('|');
        if (end == -1) {
            end = mention.indexOf('>');
        }
        if (end <= start) {
            return null;
        }
        return mention.substring(start, end);
    }

    private String stripPrefix(String value, String prefix) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.startsWith(prefix) ? value.substring(prefix.length()) : value;
    }

    private String asUserMention(String userId) {
        if (!StringUtils.hasText(userId)) {
            return "";
        }
        return "<@" + userId + ">";
    }

    private record SlackUserGroupListResponse(
            boolean ok,
            List<SlackUserGroup> usergroups
    ) {
    }

    private record SlackUserGroup(
            String id,
            String handle,
            String name
    ) {
    }

    private record SlackUserGroupUsersResponse(
            boolean ok,
            List<String> users
    ) {
    }

    private record SlackConversationListResponse(
            boolean ok,
            List<SlackConversation> channels
    ) {
    }

    private record SlackConversation(
            String id,
            String name
    ) {
    }

    private record SlackConversationMembersResponse(
            boolean ok,
            List<String> members
    ) {
    }
}
