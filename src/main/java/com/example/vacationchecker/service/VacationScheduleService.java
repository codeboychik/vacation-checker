package com.example.vacationchecker.service;

import com.example.vacationchecker.model.VacationEntry;

import java.time.LocalDate;
import java.util.List;

public interface VacationScheduleService {
    List<VacationEntry> findUpcomingVacations(String userMention, LocalDate startInclusive, LocalDate endInclusive);
}
