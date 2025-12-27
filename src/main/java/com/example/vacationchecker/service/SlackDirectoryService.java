package com.example.vacationchecker.service;

import java.util.List;
import java.util.Optional;

public interface SlackDirectoryService {

    boolean isGroup(String mention);

    boolean isChannel(String mention);

    List<String> resolveMembers(String mention);

    Optional<String> resolveUserEmail(String mention);
}
