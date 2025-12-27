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
        @JsonAlias({"startDate", "start_date"})
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate startDate,
        @JsonAlias({"endDate", "end_date"})
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate endDate,
        String description,
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
}
