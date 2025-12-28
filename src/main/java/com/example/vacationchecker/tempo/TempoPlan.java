package com.example.vacationchecker.tempo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TempoPlan(
        String self,
        String id,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate startDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate endDate,
        String createdAt,
        String updatedAt,
        Integer plannedSecondsPerDay,
        Integer totalPlannedSeconds,
        String effortPersistenceType,
        String syncSource,
        Boolean includeNonWorkingDays,
        String rule,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate recurrenceEndDate,
        String startTime,
        TempoPlanUser assignee,
        TempoPlanItem planItem,
        TempoPlannedTime plannedTime,
        Integer totalPlannedSecondsInScope,
        TempoPlanApproval planApproval,
        String planCreatorId
) {
}
