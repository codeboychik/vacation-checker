# Integration configuration

Use these settings when replacing the sample in-memory services with real Slack and Tempo Planner backends. Copy `env.example` to `.env` and populate the variables below so Spring can read them at startup.

## Slack app configuration
- **Signing Secret**: Needed to verify `/vacation` requests (`SLACK_SIGNING_SECRET`).
- **Bot/User OAuth Token**: Token with permissions to resolve users, user groups, and channel members (`SLACK_BOT_TOKEN`).
- **Slash command URL**: Point `/vacation` to `https://<your-domain>/slack/command`.
- **Scopes**: Grant `commands`, `users:read`, `users:read.email`, `usergroups:read`, and `channels:read` (or `groups:read` for private channels) so the directory service can map `@user`, `@group`, and `#channel` to member IDs.
- **App-level tokens (optional)**: If you switch to the Events API to refresh memberships asynchronously, configure a socket mode/app-level token with `connections:write`.

## Jira / Tempo Planner configuration
- **Base URL**: Your Jira Cloud or Server base URL (`JIRA_BASE_URL`).
- **Authentication**: Email/username and API token or PAT (`JIRA_USERNAME`, `JIRA_API_TOKEN`).
- **Project/source selection**: Project keys or boards that hold vacation/OOO tickets (`JIRA_PROJECT_KEYS`).
- **JQL for upcoming approved time off**: Query that filters to the coming month and reviewer-approved issues, e.g., `project in (VAC) AND status = Approved AND startDate >= now() AND startDate < startOfMonth(+2)`; adjust field names to match your schema.
- **Custom fields**: Field IDs/names for start date, end date, assignee, and reviewer status if they differ from Jira defaults (`JIRA_START_FIELD`, `JIRA_END_FIELD`, `JIRA_REVIEW_STATUS_FIELD`).
## Tempo Planner (Capacity Planner) configuration
- **Base URL**: Tempo API base URL (`CAPACITY_PLANNER_BASE_URL`), for example `https://api.tempo.io`.
- **API token**: Tempo API token (`CAPACITY_PLANNER_TOKEN`).
- **Plans endpoint path**: Planner API path for plans (`CAPACITY_PLANNER_PLANS_PATH`), defaulting to `/planning/1/plans` if unset.
- **Schedule/team filter**: Optional schedule identifier if your Tempo instance requires it (`CAPACITY_PLANNER_SCHEDULE_ID`).
- **Approval filter**: Status values that should count as approved time off (`CAPACITY_PLANNER_APPROVED_STATUSES`, comma-separated).
- **Time-off types**: Plan type values that correspond to time off (`CAPACITY_PLANNER_TIME_OFF_TYPES`, comma-separated, e.g. `TIME_OFF,ABSENCE`).

## Timezone handling
- Set the timezone used to render timelines so dates align with your Jira/Tempo Planner data (`APP_TIMEZONE`).

## Application wiring
- Implement `SlackDirectoryService` with the Slack Web API using the signing secret and bot token.
- Implement `VacationScheduleService` with Jira or Tempo Planner using the credentials and field mappings above.
- Surface each configuration as environment variables or Spring `application.yml` entries and inject them into your service beans.
