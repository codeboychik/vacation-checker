package com.example.vacationchecker.service;

import java.util.Optional;

public interface JiraUserDirectoryService {
    Optional<JiraUserProfile> resolveUserByEmail(String email);

    record JiraUserProfile(String accountId, String displayName) {
    }
}
