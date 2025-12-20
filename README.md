# vacation-checker

Spring Boot Slack slash-command app that surfaces upcoming, reviewer-approved vacation plans from Jira for individuals, Slack user groups, or channels.

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

This sample ships with in-memory data for Jira tickets and Slack membership; integrate `VacationScheduleService` and `SlackDirectoryService` with real APIs to connect to your environment.

## Integration configuration
For production hookups to Slack and Jira/Capacity Planner, capture the required tokens, URLs, and field mappings listed in [`docs/integration.md`](docs/integration.md). Plug them into your Spring configuration when you replace the in-memory services with real clients.

## Environment variables
Copy `env.example` to `.env` (or export the variables in your shell) and replace the placeholders with your real Slack and Jira/Capacity Planner credentials. Spring Boot will automatically consume these environment variables when you wire the production integrations.

The application reads the values at startup to configure its timezone window for the `/vacation` command and logs which Slack/Jira/Capacity Planner endpoints are wired (masking secrets). Set `APP_TIMEZONE` to the zone you want to use when computing the one-month lookahead.
