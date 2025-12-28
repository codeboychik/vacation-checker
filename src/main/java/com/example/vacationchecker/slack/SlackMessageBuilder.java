package com.example.vacationchecker.slack;

import com.example.vacationchecker.model.VacationEntry;
import com.example.vacationchecker.model.VacationTimeline;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SlackMessageBuilder {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d");
    private static final int TIMELINE_DAYS = 30;
    private static final String FREE_EMOJI = "⬜";
    private static final String VACATION_EMOJI = "🟩";

    private final Clock clock;

    public SlackMessageBuilder(Clock clock) {
        this.clock = clock;
    }

    public SlackResponse buildResponse(List<VacationTimeline> timelines) {
        List<Map<String, Object>> blocks = new ArrayList<>();
        if (timelines.isEmpty()) {
            blocks.add(textSection("No upcoming vacations in the next month."));
            return new SlackResponse("in_channel", blocks);
        }

        for (VacationTimeline timeline : timelines) {
            blocks.add(header("Vacation timeline for " + timeline.subject() + ":"));
            blocks.add(textSection(buildCalendarStrip(timeline.entries())));
            blocks.add(textSection("Legend: " + FREE_EMOJI + " = free, " + VACATION_EMOJI + " = vacation"));
            blocks.add(divider());
        }

        if (!blocks.isEmpty()) {
            blocks.remove(blocks.size() - 1);
        }
        return new SlackResponse("in_channel", blocks);
    }

    private Map<String, Object> header(String text) {
        Map<String, Object> block = new HashMap<>();
        block.put("type", "header");
        block.put("text", Map.of("type", "plain_text", "text", text));
        return block;
    }

    private Map<String, Object> textSection(String text) {
        Map<String, Object> block = new HashMap<>();
        block.put("type", "section");
        block.put("text", Map.of("type", "mrkdwn", "text", text));
        return block;
    }

    private Map<String, Object> divider() {
        return Map.of("type", "divider");
    }

    private String buildCalendarStrip(List<VacationEntry> entries) {
        LocalDate start = LocalDate.now(clock);
        LocalDate end = start.plusDays(TIMELINE_DAYS - 1L);
        List<String> lines = new ArrayList<>();
        LocalDate cursor = start;
        while (!cursor.isAfter(end)) {
            int remaining = (int) (end.toEpochDay() - cursor.toEpochDay()) + 1;
            int span = Math.min(7, remaining);
            LocalDate weekStart = cursor;
            LocalDate weekEnd = cursor.plusDays(span - 1L);
            String range = DATE_FORMAT.format(weekStart) + "–" + DATE_FORMAT.format(weekEnd);
            StringBuilder line = new StringBuilder();
            line.append(String.format("%-12s", range))
                    .append(" ");
            for (int i = 0; i < span; i++) {
                LocalDate day = weekStart.plusDays(i);
                line.append(isVacationDay(day, entries) ? VACATION_EMOJI : FREE_EMOJI);
            }
            lines.add(line.toString());
            cursor = weekEnd.plusDays(1L);
        }
        return "```" + String.join("\n\n", lines) + "```";
    }

    private boolean isVacationDay(LocalDate day, List<VacationEntry> entries) {
        for (VacationEntry entry : entries) {
            if ((day.isEqual(entry.startDate()) || day.isAfter(entry.startDate()))
                    && (day.isEqual(entry.endDate()) || day.isBefore(entry.endDate()))) {
                return true;
            }
        }
        return false;
    }
}
