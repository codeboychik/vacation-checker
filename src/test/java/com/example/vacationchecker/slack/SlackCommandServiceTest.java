package com.example.vacationchecker.slack;

import com.example.vacationchecker.model.VacationEntry;
import com.example.vacationchecker.service.SlackDirectoryService;
import com.example.vacationchecker.service.VacationScheduleService;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SlackCommandServiceTest {

    private final SlackCommandService service = new SlackCommandService(
            new TestSlackDirectoryService(),
            new TestVacationScheduleService(),
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

    private static final class TestSlackDirectoryService implements SlackDirectoryService {

        @Override
        public boolean isGroup(String mention) {
            return "@backend-team".equalsIgnoreCase(mention) || "@unknown-team".equalsIgnoreCase(mention);
        }

        @Override
        public boolean isChannel(String mention) {
            return "#platform".equalsIgnoreCase(mention);
        }

        @Override
        public List<String> resolveMembers(String mention) {
            if ("@backend-team".equalsIgnoreCase(mention)) {
                return List.of("@alice", "@bob");
            }
            if ("#platform".equalsIgnoreCase(mention)) {
                return List.of("@alice", "@carol");
            }
            return List.of();
        }

        @Override
        public Optional<String> resolveUserEmail(String mention) {
            return Optional.empty();
        }
    }

    private static final class TestVacationScheduleService implements VacationScheduleService {

        @Override
        public List<VacationEntry> findUpcomingVacations(String userMention, LocalDate startInclusive,
                                                         LocalDate endInclusive) {
            if ("@alice".equalsIgnoreCase(userMention)) {
                return List.of(new VacationEntry("VAC-101", "Conference", "@lead",
                        startInclusive.plusDays(2), startInclusive.plusDays(4), "Approved"));
            }
            if ("@bob".equalsIgnoreCase(userMention)) {
                return List.of(new VacationEntry("VAC-110", "Family trip", "@lead",
                        startInclusive.plusDays(9), startInclusive.plusDays(12), "Approved"));
            }
            if ("@carol".equalsIgnoreCase(userMention)) {
                return List.of(new VacationEntry("VAC-120", "Conference", "@supervisor",
                        startInclusive.plusDays(7), startInclusive.plusDays(8), "Approved"));
            }
            return List.of();
        }
    }
}
