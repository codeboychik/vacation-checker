# vacation-checker

Spring Boot Slack slash-command app that surfaces upcoming, reviewer-approved vacation plans from Tempo Planner (Capacity Planner) for individuals, Slack user groups, or channels.

## Running locally

1. Build and run the application:
   ```bash
   ./mvnw spring-boot:run
   ```
2. Expose `POST /slack/command` to Slack using your tunneling tool of choice. Configure your `/vacation` slash command to send the `text` (e.g., `@alice`, `@backend-team`, or `#platform`).

## What it does
- `/vacation @user` — returns a timeline for the mentioned user.
- `/vacation @group` — resolves the Slack user group, and returns a timeline for each member in one response.
- `/vacation #channel` — resolves the channel to its members and returns their timelines in the same response.

This sample ships with in-memory data for Slack membership, and it uses the Tempo Planner API for real vacation timelines. You can still wire Jira-based time-off logic by swapping the `VacationScheduleService` implementation.

## Integration configuration
For production hookups to Slack and Tempo Planner, capture the required tokens, URLs, and field mappings listed in [`docs/integration.md`](docs/integration.md).

## Environment variables
Copy `env.example` to `.env` (or export the variables in your shell) and replace the placeholders with your real Slack, Jira (optional), and Tempo Planner credentials. Spring Boot will automatically consume these environment variables when you wire the production integrations.

The application reads the values at startup to configure its timezone window for the `/vacation` command and logs which Slack/Tempo Planner endpoints are wired (masking secrets). Set `APP_TIMEZONE` to the zone you want to use when computing the one-month lookahead.
