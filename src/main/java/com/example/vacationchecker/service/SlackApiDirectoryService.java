package com.example.vacationchecker.service;

import com.example.vacationchecker.config.SlackProperties;
import com.fasterxml.jackson.annotation.JsonAlias;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class SlackApiDirectoryService implements SlackDirectoryService {

    private static final String SLACK_API_BASE = "https://slack.com/api";
    private static final String USERGROUP_PREFIX = "<!subteam^";
    private static final String CHANNEL_PREFIX = "<#";
    private static final String USER_PREFIX = "<@";

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

    @Override
    public Optional<String> resolveUserEmail(String mention) {
        if (!StringUtils.hasText(mention) || !StringUtils.hasText(slackProperties.botToken())) {
            return Optional.empty();
        }
        String userId = extractUserId(mention);
        if (!StringUtils.hasText(userId)) {
            String handle = stripPrefix(mention, "@");
            userId = resolveUserIdByHandle(handle);
        }
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
        if (response == null || !response.ok() || response.user() == null) {
            return Optional.empty();
        }
        SlackUserProfile profile = response.user().profile();
        if (profile == null || !StringUtils.hasText(profile.email())) {
            return Optional.empty();
        }
        return Optional.of(profile.email());
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
        SlackUserGroupUsersResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/usergroups.users.list")
                        .queryParam("usergroup", groupId)
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
        SlackConversationMembersResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/conversations.members")
                        .queryParam("channel", channelId)
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

    private String resolveUserIdByHandle(String handle) {
        if (!StringUtils.hasText(handle)) {
            return null;
        }
        SlackUserListResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/users.list")
                        .queryParam("limit", 1000)
                        .build())
                .headers(this::applyAuth)
                .retrieve()
                .body(SlackUserListResponse.class);
        if (response == null || !response.ok() || response.members() == null) {
            return null;
        }
        String normalizedHandle = handle.toLowerCase(Locale.ROOT);
        return response.members().stream()
                .filter(member -> handleMatches(member, normalizedHandle))
                .map(SlackUser::id)
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

    private String extractUserId(String mention) {
        if (!StringUtils.hasText(mention)) {
            return null;
        }
        if (mention.startsWith(USER_PREFIX)) {
            int start = USER_PREFIX.length();
            int end = mention.indexOf('|');
            if (end == -1) {
                end = mention.indexOf('>');
            }
            if (end <= start) {
                return null;
            }
            return mention.substring(start, end);
        }
        if (mention.startsWith("@")) {
            return null;
        }
        return mention;
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

    private boolean handleMatches(SlackUser member, String normalizedHandle) {
        if (member == null) {
            return false;
        }
        if (StringUtils.hasText(member.name()) && member.name().equalsIgnoreCase(normalizedHandle)) {
            return true;
        }
        SlackUserProfile profile = member.profile();
        if (profile == null) {
            return false;
        }
        return equalsIgnoreCase(profile.displayName(), normalizedHandle)
                || equalsIgnoreCase(profile.realName(), normalizedHandle);
    }

    private boolean equalsIgnoreCase(String value, String otherLowercased) {
        return StringUtils.hasText(value) && value.toLowerCase(Locale.ROOT).equals(otherLowercased);
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

    private record SlackUserListResponse(
            boolean ok,
            List<SlackUser> members
    ) {
    }

    private record SlackUserInfoResponse(
            boolean ok,
            SlackUser user
    ) {
    }

    private record SlackUser(
            String id,
            String name,
            SlackUserProfile profile
    ) {
    }

    private record SlackUserProfile(
            String email,
            @JsonAlias("display_name")
            String displayName,
            @JsonAlias("real_name")
            String realName
    ) {
    }
}
