package com.example.vacationchecker.slack;

import com.example.vacationchecker.service.InMemorySlackDirectoryService;
import com.example.vacationchecker.service.InMemoryVacationScheduleService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class SlackCommandServiceTest {

    private final SlackCommandService service = new SlackCommandService(
            new InMemorySlackDirectoryService(),
            new InMemoryVacationScheduleService(),
            new SlackMessageBuilder(),
            Clock.fixed(Instant.parse("2024-05-01T00:00:00Z"), ZoneOffset.UTC)
    );

    @Test
    void buildsTimelineForSingleUser() {
        SlackResponse response = service.handleVacationCommand("@alice");

        assertThat(response.getBlocks()).anySatisfy(block ->
                assertThat(block.get("text").toString()).contains("@alice"));
    }

    @Test
    void buildsTimelineForGroup() {
        SlackResponse response = service.handleVacationCommand("@backend-team");

        long headers = response.getBlocks().stream()
                .filter(block -> "header".equals(block.get("type")))
                .count();

        assertThat(headers).isEqualTo(2);
    }

    @Test
    void handlesUnknownGroupWithEmptyTimeline() {
        SlackResponse response = service.handleVacationCommand("@unknown-team");

        assertThat(response.getBlocks()).hasSize(2);
        assertThat(response.getBlocks().get(0).get("text").toString()).contains("@unknown-team");
    }
}
