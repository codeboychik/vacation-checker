package com.example.vacationchecker.model;

import java.util.List;

public record VacationTimeline(String subject, TargetType targetType, List<VacationEntry> entries) {
}
