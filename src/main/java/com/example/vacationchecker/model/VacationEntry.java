package com.example.vacationchecker.model;

import java.time.LocalDate;

public record VacationEntry(String ticketKey, String summary, String reviewer, LocalDate startDate,
                            LocalDate endDate, String status) {
}
