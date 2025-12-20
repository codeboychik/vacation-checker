package com.example.vacationchecker.slack;

import com.example.vacationchecker.model.VacationEntry;
import com.example.vacationchecker.model.VacationTimeline;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SlackMessageBuilder {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d");

    public SlackResponse buildResponse(List<VacationTimeline> timelines) {
        List<Map<String, Object>> blocks = new ArrayList<>();
        if (timelines.isEmpty()) {
            blocks.add(textSection("No upcoming vacations in the next month."));
            return new SlackResponse("in_channel", blocks);
        }

        for (VacationTimeline timeline : timelines) {
            blocks.add(header("Vacation timeline for " + timeline.subject()));
            if (timeline.entries().isEmpty()) {
                blocks.add(textSection("• No approved time off in the next month."));
            } else {
                for (VacationEntry entry : timeline.entries()) {
                    blocks.add(textSection(formatEntry(entry)));
                    blocks.add(context("Reviewer: " + entry.reviewer() + " • Status: " + entry.status()));
                }
            }
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

    private Map<String, Object> context(String text) {
        Map<String, Object> block = new HashMap<>();
        block.put("type", "context");
        block.put("elements", List.of(Map.of("type", "mrkdwn", "text", text)));
        return block;
    }

    private Map<String, Object> divider() {
        return Map.of("type", "divider");
    }

    private String formatEntry(VacationEntry entry) {
        return "• " + DATE_FORMAT.format(entry.startDate()) + " – " + DATE_FORMAT.format(entry.endDate()) +
                " | " + entry.ticketKey() + " " + entry.summary();
    }
}
