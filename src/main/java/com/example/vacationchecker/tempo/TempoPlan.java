package com.example.vacationchecker.tempo;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TempoPlan(
        String id,
        @JsonAlias({"planId", "plan_id"})
        String planId,
        @JsonAlias({"issueKey", "issue_key"})
        String issueKey,
        TempoPlanIssue issue,
        TempoPlanUser assignee,
        @JsonAlias({"planApproval", "plan_approval"})
        TempoPlanApproval planApproval,
        @JsonAlias({"planItem", "plan_item"})
        TempoPlanItem planItem,
        @JsonAlias({"startDate", "start_date"})
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate startDate,
        @JsonAlias({"endDate", "end_date"})
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate endDate,
        String createdAt,
        String updatedAt,
        String description,
        String planCreatorId,
        String rule,
        String syncSource,
        String startTime,
        String recurrenceEndDate,
        @JsonAlias({"includeNonWorkingDays", "include_non_working_days"})
        Boolean includeNonWorkingDays,
        @JsonAlias({"plannedSecondsPerDay", "planned_seconds_per_day"})
        Integer plannedSecondsPerDay,
        @JsonAlias({"totalPlannedSeconds", "total_planned_seconds"})
        Integer totalPlannedSeconds,
        @JsonAlias({"totalPlannedSecondsInScope", "total_planned_seconds_in_scope"})
        Integer totalPlannedSecondsInScope,
        @JsonAlias({"plannedTime", "planned_time"})
        TempoPlannedTime plannedTime,
        @JsonAlias({"approvalStatus", "approval_status"})
        String approvalStatus,
        String status,
        @JsonAlias({"planType", "plan_type"})
        String planType,
        @JsonAlias({"planItemType", "plan_item_type"})
        String planItemType,
        String type,
        String classification,
        @JsonAlias({"approvedBy", "approved_by"})
        String approvedBy
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TempoPlanApproval(
            TempoPlanApprovalUser actor,
            TempoPlanApprovalUser requester,
            TempoPlanApprovalUser reviewer,
            String status
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TempoPlanApprovalUser(
            @JsonAlias({"accountId", "account_id"})
            String accountId,
            String self
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TempoPlanItem(
            String id,
            String self,
            String type
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TempoPlannedTime(
            TempoPlannedTimeDays days,
            TempoPlannedTimePeriods periods,
            TempoPlannedTimeMetadata metadata
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TempoPlannedTimeDays(
            Integer count,
            java.util.List<TempoPlannedTimeDayValue> values
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TempoPlannedTimeDayValue(
            String date,
            Integer plannedSeconds
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TempoPlannedTimePeriods(
            Integer count,
            java.util.List<TempoPlannedTimePeriodValue> values
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TempoPlannedTimePeriodValue(
            String from,
            String to,
            Integer plannedSeconds
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TempoPlannedTimeMetadata(
            String all
    ) {
    }
}
