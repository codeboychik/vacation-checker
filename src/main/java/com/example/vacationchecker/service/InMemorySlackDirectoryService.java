package com.example.vacationchecker.service;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class InMemorySlackDirectoryService implements SlackDirectoryService {

    private final Map<String, List<String>> groups = Map.of(
            "@backend-team", List.of("@alice", "@bob"),
            "@qa-team", List.of("@carol", "@dave")
    );

    private final Map<String, List<String>> channels = Map.of(
            "#platform", List.of("@alice", "@carol"),
            "#support", List.of("@bob", "@dave")
    );

    @Override
    public boolean isGroup(String mention) {
        return groups.containsKey(mention.toLowerCase());
    }

    @Override
    public boolean isChannel(String mention) {
        return channels.containsKey(mention.toLowerCase());
    }

    @Override
    public List<String> resolveMembers(String mention) {
        if (isGroup(mention)) {
            return groups.get(mention.toLowerCase());
        }
        if (isChannel(mention)) {
            return channels.get(mention.toLowerCase());
        }
        return Collections.emptyList();
    }
}
