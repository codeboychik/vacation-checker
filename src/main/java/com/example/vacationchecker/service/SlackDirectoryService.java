package com.example.vacationchecker.service;

import java.util.List;

public interface SlackDirectoryService {

    boolean isGroup(String mention);

    boolean isChannel(String mention);

    List<String> resolveMembers(String mention);
}
