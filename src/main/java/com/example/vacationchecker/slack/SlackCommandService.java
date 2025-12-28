package com.example.vacationchecker.slack;

import com.example.vacationchecker.model.CommandTarget;
import com.example.vacationchecker.model.TargetType;
import com.example.vacationchecker.model.VacationTimeline;
import com.example.vacationchecker.service.JiraUserDirectoryService;
import com.example.vacationchecker.service.SlackDirectoryService;
import com.example.vacationchecker.service.SlackUserInfoService;
import com.example.vacationchecker.service.VacationScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SlackCommandService {

    private final SlackDirectoryService directoryService;
    private final SlackUserInfoService slackUserInfoService;
    private final JiraUserDirectoryService jiraUserDirectoryService;
    private final VacationScheduleService vacationScheduleService;
    private final SlackMessageBuilder messageBuilder;
    private final Clock clock;

    public SlackCommandService(SlackDirectoryService directoryService,
                               SlackUserInfoService slackUserInfoService,
                               JiraUserDirectoryService jiraUserDirectoryService,
                               VacationScheduleService vacationScheduleService,
                               SlackMessageBuilder messageBuilder,
                               Clock clock) {
        this.directoryService = directoryService;
        this.slackUserInfoService = slackUserInfoService;
        this.jiraUserDirectoryService = jiraUserDirectoryService;
        this.vacationScheduleService = vacationScheduleService;
        this.messageBuilder = messageBuilder;
        this.clock = clock;
    }

    public SlackResponse handleVacationCommand(String text) {
        CommandTarget target = parseTarget(text);
        LocalDate now = LocalDate.now(clock);
        LocalDate nextMonth = now.plusMonths(1);

        List<VacationTimeline> timelines = switch (target.type()) {
            case USER -> List.of(buildTimelineForUser(target.identifier(), now, nextMonth));
            case GROUP, CHANNEL -> buildTimelinesForCollection(target, now, nextMonth);
        };

        return messageBuilder.buildResponse(timelines);
    }

    private CommandTarget parseTarget(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("No target provided. Usage: /vacation @user|@group|#channel");
        }
        if (directoryService.isChannel(trimmed)) {
            return new CommandTarget(TargetType.CHANNEL, trimmed);
        }
        if (directoryService.isGroup(trimmed)) {
            return new CommandTarget(TargetType.GROUP, trimmed);
        }
        if (trimmed.startsWith("<@") || trimmed.startsWith("@")) {
            return new CommandTarget(TargetType.USER, trimmed);
        }
        if (trimmed.startsWith("#")) {
            return new CommandTarget(TargetType.CHANNEL, trimmed);
        }
        throw new IllegalArgumentException("Unsupported mention type: " + trimmed);
    }

    private VacationTimeline buildTimelineForUser(String userMention, LocalDate start, LocalDate end) {
        Optional<String> email = slackUserInfoService.resolveEmail(userMention);
        Optional<JiraUserDirectoryService.JiraUserProfile> profile = email.flatMap(jiraUserDirectoryService::resolveUserByEmail);
        if (profile.isEmpty()) {
            return new VacationTimeline(userMention, TargetType.USER, List.of());
        }
        JiraUserDirectoryService.JiraUserProfile userProfile = profile.get();
        String subject = StringUtils.hasText(userProfile.displayName()) ? userProfile.displayName() : userMention;
        return new VacationTimeline(subject, TargetType.USER,
                vacationScheduleService.findUpcomingVacations(userProfile.accountId(), start, end));
    }

    private List<VacationTimeline> buildTimelinesForCollection(CommandTarget target, LocalDate start, LocalDate end) {
        List<String> members = directoryService.resolveMembers(target.identifier());
        List<VacationTimeline> timelines = new ArrayList<>();
        if (members.isEmpty()) {
            timelines.add(new VacationTimeline(target.identifier(), target.type(), List.of()));
            return timelines;
        }
        for (String member : members) {
            timelines.add(buildTimelineForUser(member, start, end));
        }
        return timelines;
    }
}
