package com.example.vacationchecker.service;

import com.example.vacationchecker.model.VacationEntry;
import com.example.vacationchecker.tempo.TempoPlan;
import com.example.vacationchecker.tempo.TempoPlannerClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class TempoVacationScheduleService implements VacationScheduleService {

    private final TempoPlannerClient plannerClient;

    public TempoVacationScheduleService(TempoPlannerClient plannerClient) {
        this.plannerClient = plannerClient;
    }

    @Override
    public List<VacationEntry> findUpcomingVacations(String accountId, LocalDate startInclusive,
                                                     LocalDate endInclusive) {
        if (!StringUtils.hasText(accountId)) {
            return List.of();
        }
        return plannerClient.fetchPlans(accountId, startInclusive, endInclusive).stream()
                .filter(plan -> plan.startDate() != null && plan.endDate() != null)
                .filter(this::matchesApprovalStatus)
                .map(this::toVacationEntry)
                .sorted(Comparator.comparing(VacationEntry::startDate))
                .toList();
    }

    private boolean matchesApprovalStatus(TempoPlan plan) {
        String planStatus = firstNonBlank(
                plan.planApproval() != null ? plan.planApproval().status() : null
        );
        if (!StringUtils.hasText(planStatus)) {
            return false;
        }
        return "approved".equalsIgnoreCase(planStatus);
    }

    private VacationEntry toVacationEntry(TempoPlan plan) {
        String issueKey = firstNonBlank(
                plan.planItem() != null ? plan.planItem().id() : null,
                plan.id(),
                "TIME-OFF"
        );
        String summary = "Planned time off";
        String reviewer = firstNonBlank(
                plan.planApproval() != null && plan.planApproval().reviewer() != null
                        ? plan.planApproval().reviewer().accountId()
                        : null,
                "Tempo Planner"
        );
        String status = firstNonBlank(
                plan.planApproval() != null ? plan.planApproval().status() : null,
                "Planned"
        );
        return new VacationEntry(issueKey, summary, reviewer, plan.startDate(), plan.endDate(), status);
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
