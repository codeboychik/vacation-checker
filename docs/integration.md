# Integration configuration

Use these settings when replacing the sample in-memory services with real Slack and Jira/Capacity Planner backends. Copy `env.example` to `.env` and populate the variables below so Spring can read them at startup.

## Slack app configuration
- **Signing Secret**: Needed to verify `/vacation` requests (`SLACK_SIGNING_SECRET`).
- **Bot/User OAuth Token**: Token with permissions to resolve users, user groups, and channel members (`SLACK_BOT_TOKEN`).
- **Slash command URL**: Point `/vacation` to `https://<your-domain>/slack/command`.
- **Scopes**: Grant `commands`, `users:read`, `users:read.email`, `usergroups:read`, and `channels:read` (or `groups:read` for private channels) so the directory service can map `@user`, `@group`, and `#channel` to member IDs.
- **App-level tokens (optional)**: If you switch to the Events API to refresh memberships asynchronously, configure a socket mode/app-level token with `connections:write`.

## Jira / Capacity Planner configuration
- **Base URL**: Your Jira Cloud or Server base URL (`JIRA_BASE_URL`).
- **Authentication**: Email/username and API token or PAT (`JIRA_USERNAME`, `JIRA_API_TOKEN`).
- **Project/source selection**: Project keys or boards that hold vacation/OOO tickets (`JIRA_PROJECT_KEYS`).
- **JQL for upcoming approved time off**: Query that filters to the coming month and reviewer-approved issues, e.g., `project in (VAC) AND status = Approved AND startDate >= now() AND startDate < startOfMonth(+2)`; adjust field names to match your schema.
- **Custom fields**: Field IDs/names for start date, end date, assignee, and reviewer status if they differ from Jira defaults (`JIRA_START_FIELD`, `JIRA_END_FIELD`, `JIRA_REVIEW_STATUS_FIELD`).
- **Capacity planner source**: If vacation data lives outside Jira, capture its base URL, token, and any schedule identifier needed to fetch the upcoming month (for example, `CAPACITY_PLANNER_BASE_URL`, `CAPACITY_PLANNER_TOKEN`, `CAPACITY_PLANNER_SCHEDULE_ID`).
- **Timezone handling**: Set the timezone used to render timelines so dates align with your Jira/Capacity Planner data (`APP_TIMEZONE`).

## Application wiring
- Implement `SlackDirectoryService` with the Slack Web API using the signing secret and bot token.
- Implement `VacationScheduleService` with Jira or your capacity planner using the credentials and field mappings above.
- Surface each configuration as environment variables or Spring `application.yml` entries and inject them into your service beans.
