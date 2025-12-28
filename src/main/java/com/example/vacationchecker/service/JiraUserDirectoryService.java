package com.example.vacationchecker.service;

import java.util.Optional;

public interface JiraUserDirectoryService {
    Optional<String> resolveAccountIdByEmail(String email);
}
