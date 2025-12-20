package com.example.vacationchecker.service;

import com.example.vacationchecker.model.VacationEntry;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryVacationScheduleService implements VacationScheduleService {

    private final Map<String, List<VacationEntry>> vacationsByUser = Map.of(
            "@alice", List.of(
                    new VacationEntry("VAC-101", "Conference", "@lead", LocalDate.now().plusDays(3),
                            LocalDate.now().plusDays(5), "Approved"),
                    new VacationEntry("VAC-105", "Summer Break", "@manager", LocalDate.now().plusDays(25),
                            LocalDate.now().plusDays(30), "Awaiting Approval")
            ),
            "@bob", List.of(
                    new VacationEntry("VAC-110", "Family trip", "@lead", LocalDate.now().plusDays(10),
                            LocalDate.now().plusDays(14), "Approved")
            ),
            "@carol", List.of(
                    new VacationEntry("VAC-120", "Conference", "@supervisor", LocalDate.now().plusDays(8),
                            LocalDate.now().plusDays(9), "Approved")
            ),
            "@dave", List.of(
                    new VacationEntry("VAC-130", "Training", "@supervisor", LocalDate.now().plusDays(18),
                            LocalDate.now().plusDays(20), "Approved")
            )
    );

    @Override
    public List<VacationEntry> findUpcomingVacations(String userMention, LocalDate startInclusive, LocalDate endInclusive) {
        return vacationsByUser.getOrDefault(userMention.toLowerCase(), List.of()).stream()
                .filter(entry -> isWithinRange(entry, startInclusive, endInclusive))
                .sorted(Comparator.comparing(VacationEntry::startDate))
                .toList();
    }

    private boolean isWithinRange(VacationEntry entry, LocalDate startInclusive, LocalDate endInclusive) {
        return (entry.startDate().isAfter(startInclusive.minusDays(1)) && entry.startDate().isBefore(endInclusive.plusDays(1)))
                || (entry.endDate().isAfter(startInclusive.minusDays(1)) && entry.endDate().isBefore(endInclusive.plusDays(1)));
    }
}
