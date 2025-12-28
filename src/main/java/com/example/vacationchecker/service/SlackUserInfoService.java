package com.example.vacationchecker.service;

import java.util.Optional;

public interface SlackUserInfoService {
    Optional<String> resolveEmail(String userMention);
}
