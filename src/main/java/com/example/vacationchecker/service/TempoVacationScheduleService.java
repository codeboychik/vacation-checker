package com.example.vacationchecker.service;

import com.example.vacationchecker.config.CapacityPlannerProperties;
import com.example.vacationchecker.model.VacationEntry;
import com.example.vacationchecker.tempo.TempoPlan;
import com.example.vacationchecker.tempo.TempoPlanIssue;
import com.example.vacationchecker.tempo.TempoPlannerClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class TempoVacationScheduleService implements VacationScheduleService {

    private final TempoPlannerClient plannerClient;
    private final CapacityPlannerProperties properties;
    private final SlackDirectoryService slackDirectoryService;
    private final JiraUserDirectoryService jiraUserDirectoryService;

    public TempoVacationScheduleService(TempoPlannerClient plannerClient,
                                        CapacityPlannerProperties properties,
                                        SlackDirectoryService slackDirectoryService,
                                        JiraUserDirectoryService jiraUserDirectoryService) {
        this.plannerClient = plannerClient;
        this.properties = properties;
        this.slackDirectoryService = slackDirectoryService;
        this.jiraUserDirectoryService = jiraUserDirectoryService;
    }

    @Override
    public List<VacationEntry> findUpcomingVacations(String userMention, LocalDate startInclusive,
                                                     LocalDate endInclusive) {
        ResolvedAssignee resolvedAssignee = resolveAssignee(userMention);
        if (!resolvedAssignee.hasQueryValue()) {
            return List.of();
        }
        return plannerClient.fetchPlans(resolvedAssignee.queryValue(), startInclusive, endInclusive).stream()
                .filter(plan -> plan.startDate() != null && plan.endDate() != null)
                .filter(plan -> matchesAssignee(plan, resolvedAssignee))
                .filter(this::matchesApprovalStatus)
                .filter(this::matchesTimeOffType)
                .map(this::toVacationEntry)
                .sorted(Comparator.comparing(VacationEntry::startDate))
                .toList();
    }

    private boolean matchesApprovalStatus(TempoPlan plan) {
        List<String> approvedStatuses = properties.approvedStatuses();
        if (approvedStatuses == null || approvedStatuses.isEmpty()) {
            return true;
        }
        String planStatus = firstNonBlank(
                plan.approvalStatus(),
                plan.planApproval() != null ? plan.planApproval().status() : null,
                plan.status());
        if (!StringUtils.hasText(planStatus)) {
            return false;
        }
        return approvedStatuses.stream()
                .filter(StringUtils::hasText)
                .anyMatch(status -> status.equalsIgnoreCase(planStatus));
    }

    private boolean matchesTimeOffType(TempoPlan plan) {
        List<String> timeOffTypes = properties.timeOffTypes();
        if (timeOffTypes == null || timeOffTypes.isEmpty()) {
            return true;
        }
        String planType = firstNonBlank(plan.planType(), plan.planItemType(), plan.type(), plan.classification());
        if (!StringUtils.hasText(planType)) {
            return false;
        }
        String normalizedPlanType = planType.toLowerCase(Locale.ROOT);
        return timeOffTypes.stream()
                .filter(StringUtils::hasText)
                .map(type -> type.toLowerCase(Locale.ROOT))
                .anyMatch(normalizedPlanType::contains);
    }

    private VacationEntry toVacationEntry(TempoPlan plan) {
        TempoPlanIssue issue = plan.issue();
        String issueKey = firstNonBlank(plan.issueKey(), issue != null ? issue.key() : null, plan.planId(), plan.id(),
                "TIME-OFF");
        String summary = firstNonBlank(issue != null ? issue.summary() : null, plan.description(), "Planned time off");
        String reviewer = firstNonBlank(plan.approvedBy(), "Tempo Planner");
        String status = firstNonBlank(
                plan.approvalStatus(),
                plan.planApproval() != null ? plan.planApproval().status() : null,
                plan.status(),
                "Planned");
        return new VacationEntry(issueKey, summary, reviewer, plan.startDate(), plan.endDate(), status);
    }

    private String normalizeAssignee(String userMention) {
        if (!StringUtils.hasText(userMention)) {
            return null;
        }
        return userMention.startsWith("@") ? userMention.substring(1) : userMention;
    }

    private ResolvedAssignee resolveAssignee(String userMention) {
        String fallbackAssignee = normalizeAssignee(userMention);
        String email = slackDirectoryService.resolveUserEmail(userMention).orElse(null);
        String accountId = StringUtils.hasText(email)
                ? jiraUserDirectoryService.findAccountIdByEmail(email).orElse(null)
                : null;
        return new ResolvedAssignee(accountId, email, fallbackAssignee);
    }

    private boolean matchesAssignee(TempoPlan plan, ResolvedAssignee assignee) {
        if (plan == null || assignee == null) {
            return false;
        }
        TempoPlanUser planAssignee = plan.assignee();
        if (StringUtils.hasText(assignee.accountId())) {
            return StringUtils.hasText(planAssignee != null ? planAssignee.accountId() : null)
                    && assignee.accountId().equals(planAssignee.accountId());
        }
        if (StringUtils.hasText(assignee.email())) {
            return StringUtils.hasText(planAssignee != null ? planAssignee.email() : null)
                    && assignee.email().equalsIgnoreCase(planAssignee.email());
        }
        if (!StringUtils.hasText(assignee.fallbackKey())) {
            return false;
        }
        return matchesFallback(planAssignee, assignee.fallbackKey());
    }

    private boolean matchesFallback(TempoPlanUser planAssignee, String fallbackKey) {
        if (planAssignee == null || !StringUtils.hasText(fallbackKey)) {
            return false;
        }
        if (StringUtils.hasText(planAssignee.accountId())
                && fallbackKey.equalsIgnoreCase(planAssignee.accountId())) {
            return true;
        }
        if (StringUtils.hasText(planAssignee.userKey()) && fallbackKey.equalsIgnoreCase(planAssignee.userKey())) {
            return true;
        }
        if (StringUtils.hasText(planAssignee.displayName())
                && fallbackKey.equalsIgnoreCase(planAssignee.displayName())) {
            return true;
        }
        return StringUtils.hasText(planAssignee.email())
                && fallbackKey.equalsIgnoreCase(planAssignee.email());
    }

    private record ResolvedAssignee(
            String accountId,
            String email,
            String fallbackKey
    ) {
        private boolean hasQueryValue() {
            return StringUtils.hasText(accountId)
                    || StringUtils.hasText(email)
                    || StringUtils.hasText(fallbackKey);
        }

        private String queryValue() {
            if (StringUtils.hasText(accountId)) {
                return accountId;
            }
            if (StringUtils.hasText(email)) {
                return email;
            }
            return fallbackKey;
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }
}
