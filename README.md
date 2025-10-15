# 📨 YouTrack → Telegram & Teams Notifier

A lightweight, multi-platform Kotlin application that bridges **YouTrack** with **Telegram** and **Microsoft Teams**, delivering real-time issue notifications and enabling interactive issue management directly from your chat.

Built as part of an internship application demonstrating practical API integration skills, modern async programming patterns, and cross-platform messenger support.

The task setup as well as the description of the internship is provided in the repository

Part 1 and 2 from the task was done using Telegram as messenger, since the internship will be focused on integration with Teams I tried implementing the solution
with Teams as well, but it turned out that part 2 was a bit more complex and required more time than the 3-day window that was given

---

## ✨ Features

### 🔔 Real-Time Notifications (Part 1 - Completed)
Automatically monitors your YouTrack instance and pushes notifications to **both Telegram and Microsoft Teams** when new issues are created:
- **Issue ID** (e.g., `MI-42`)
- **Title** with full summary
- **Current State** (Open, In Progress, Resolved, etc.)
- **Description** preview (up to 500 characters)
- **Timestamp** of notification

### 🤖 Interactive Bot Commands (Telegram Only - Part 2)
Manage YouTrack issues without leaving Telegram:

| Command                      | Description                                     | Example                                              |
|------------------------------|-------------------------------------------------|------------------------------------------------------|
| `/new "Title" "Description"` | Create a new issue in YouTrack                  | `/new "Fix login bug" "Users can't login on mobile"` |
| `/list`                      | Display the 5 most recent issues                | `/list`                                              |
| `/info ISSUE_ID`             | Get detailed information about a specific issue | `/info MI-42`                                        |
| `/help`                      | Show available commands                         | `/help`                                              |

### ⚙️ Configurable & Flexible
- **Multi-Messenger Support**: Send to Telegram, Teams, or both simultaneously
- Adjustable polling interval via environment variables
- Smart caching system (maintains last 500 seen issues to prevent duplicates)
- Graceful error handling for network failures and API errors
- Platform-specific formatting (Markdown for Telegram, MessageCards for Teams)

---

## 🛠️ Technology Stack

- **Kotlin 2.2.20** — Modern JVM language with coroutines
- **Ktor 2.3.12** — Asynchronous HTTP client for REST API calls
- **kotlinx.serialization** — Type-safe JSON serialization/deserialization
- **Coroutines** — Concurrent polling and message handling
- **Logback** — Structured logging with SLF4J
- **dotenv** — Environment configuration management
- **Telegram Bot API** — For Telegram integration
- **Microsoft Teams Incoming Webhooks** — For Teams integration

---

## 📋 Prerequisites

- **JDK 23** or higher
- **Gradle** (wrapper included)
- **YouTrack instance** with API access
- **Telegram Bot** (create via [@BotFather](https://t.me/botfather)) - *optional, can use Teams only*
- **Microsoft Teams** workspace with webhook permissions - *optional, can use Telegram only*

---

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone <your-repository-url>
cd youtrack-notifier
```

### 2. Set Up Telegram Bot (Optional - Skip if using Teams only)

#### Create a Telegram Bot
1. Open Telegram and message [@BotFather](https://t.me/botfather)
2. Send `/newbot` and follow the prompts
3. Copy the **bot token** (format: `1234567890:ABCdefGHIjklMNOpqrsTUVwxyz`)

#### Get Your Chat ID
1. Start a chat with your bot and send any message
2. Visit:
```
   https://api.telegram.org/bot<YOUR_BOT_TOKEN>/getUpdates
```
3. Look for `"chat":{"id":123456789}` in the response
4. Copy the chat ID

### 3. Set Up Microsoft Teams Webhook (Optional - Skip if using Telegram only)

#### Create an Incoming Webhook
1. Open Microsoft Teams
2. Navigate to the channel where you want notifications (or create a new one)
3. Click **•••** (three dots) next to the channel name
4. Select **Connectors** or **Workflows** (depending on your Teams version)
5. Search for **"Incoming Webhook"**
6. Click **Configure** or **Add**
7. Give it a name (e.g., "YouTrack Notifier")
8. Optionally upload an icon
9. Click **Create**
10. **Copy the webhook URL** (format: `https://yourorg.webhook.office.com/webhookb2/...`)
11. Click **Done**

⚠️ **Security Note:** Keep your webhook URL private - anyone with it can post to your channel!

### 4. Get YouTrack Credentials

#### Create a Permanent Token
1. Log into your YouTrack instance
2. Navigate to **Profile → Authentication → Tokens**
3. Click **New Token** and set permissions:
    - ✅ **Read Issues**
    - ✅ **Create Issues** (for Telegram commands)
    - ✅ **Read Project**
4. Copy the generated permanent token

#### Get Your Project ID
1. Open your project in YouTrack
2. Check the URL: `https://yourinstance.youtrack.cloud/projects/<PROJECT_ID>`
3. Or use the REST API: `GET /api/admin/projects`

### 5. Configure Environment Variables

Create a `.env` file in the project root:
```env
# ========================================
# YouTrack Configuration (REQUIRED)
# ========================================
YOUTRACK_URL=https://yourinstance.youtrack.cloud
YOUTRACK_TOKEN=perm:your_permanent_token_here
YOUTRACK_PROJECT_ID=0-1

# ========================================
# Telegram Configuration (OPTIONAL)
# ========================================
TELEGRAM_BOT_TOKEN=1234567890:ABCdefGHIjklMNOpqrsTUVwxyz
TELEGRAM_CHAT_ID=123456789

# ========================================
# Microsoft Teams Configuration (OPTIONAL)
# ========================================
TEAMS_WEBHOOK_URL=https://yourorg.webhook.office.com/webhookb2/...

# ========================================
# Messenger Mode (REQUIRED)
# ========================================
# Options: "telegram", "teams", or "both"
MESSENGER_MODE=both

# ========================================
# Optional Settings
# ========================================
POLL_INTERVAL_SECONDS=60
```

**⚠️ Security Note:** Never commit your `.env` file to version control. Add it to `.gitignore`.

#### Configuration Options

| Variable                | Required | Default | Description                                                 |
|-------------------------|----------|---------|-------------------------------------------------------------|
| `YOUTRACK_URL`          | ✅        | -       | Base URL of your YouTrack instance (without trailing slash) |
| `YOUTRACK_TOKEN`        | ✅        | -       | Permanent token for API authentication                      |
| `YOUTRACK_PROJECT_ID`   | ✅        | -       | ID of the project to monitor                                |
| `TELEGRAM_BOT_TOKEN`    | ⚠️       | -       | Required if using Telegram                                  |
| `TELEGRAM_CHAT_ID`      | ⚠️       | -       | Required if using Telegram                                  |
| `TEAMS_WEBHOOK_URL`     | ⚠️       | -       | Required if using Teams                                     |
| `MESSENGER_MODE`        | ✅        | `both`  | Which messenger(s) to use: `telegram`, `teams`, or `both`   |
| `POLL_INTERVAL_SECONDS` | ❌        | 60      | How often to check YouTrack for new issues                  |

### 6. Build and Run

#### Using Gradle Wrapper (Recommended)
```bash
# Build the project
./gradlew build

# Run the application
./gradlew run
```

#### Using IntelliJ IDEA
1. Open the project in IntelliJ IDEA
2. Wait for Gradle sync to complete
3. Run `Main.kt`

---

## 📖 Usage

### Starting the Application

Once running, you'll see logs like:
```
🚀 YouTrack Notifier started!
📡 Polling every 60 seconds...
📨 Active messengers: Telegram Teams
```

### Receiving Notifications

#### In Telegram
When a new issue is created in YouTrack, you'll receive something like:
```
🆕 New YouTrack Issue
ID: MI-42
Title: Fix authentication bug
State: Open
Description: Users are unable to log in using OAuth...
⏰ 2025-10-15T14:23:45Z
```

#### In Microsoft Teams
You'll see a formatted MessageCard with:
- **Header**: "🆕 New YouTrack Issue"
- **Activity Title**: Issue title
- **Activity Subtitle**: Issue ID
- **Facts**: State and timestamp
- **Description**: Full description text
- **Color-coded theme**: Orange for Open, Green for Resolved, Gray for Closed

### Using Telegram Bot Commands

Simply send commands to your bot in Telegram:

**Create an issue:**
```
![/new](https://github.com/magosmihajlo/YouTrack-Integration-with-Microsoft-Teams-Internship/blob/818b71e00d7059b9c9d97c47c26bc16d1c9c773f/new.png)
```

**List recent issues:**
```
[/list](https://github.com/magosmihajlo/YouTrack-Integration-with-Microsoft-Teams-Internship/blob/818b71e00d7059b9c9d97c47c26bc16d1c9c773f/list.png)```

**Get issue details:**
```
[/info](https://github.com/magosmihajlo/YouTrack-Integration-with-Microsoft-Teams-Internship/blob/818b71e00d7059b9c9d97c47c26bc16d1c9c773f/info.png)```

**Show help:**
```
![/help](https://github.com/magosmihajlo/YouTrack-Integration-with-Microsoft-Teams-Internship/blob/818b71e00d7059b9c9d97c47c26bc16d1c9c773f/help.png)
```

---

## 🏗️ Project Structure
```
src/main/kotlin/
├── Main.kt                    # Application entry point & notification loop
├── config/
│   └── Config.kt             # Environment configuration loader
├── youtrack/
│   └── YouTrackClient.kt     # YouTrack REST API client
├── telegram/
│   └── TelegramClient.kt     # Telegram Bot API client (notifications + commands)
└── teams/
    └── TeamsClient.kt        # Microsoft Teams Webhook client (notifications only)
```

### Key Components

**Main.kt**
- Initializes concurrent coroutines:
    - **Notifier**: Polls YouTrack for new issues every N seconds
    - **Telegram Handler**: Listens for Telegram commands (if enabled)
- Routes notifications to configured messengers in parallel
- Manages issue cache to prevent duplicate notifications

**YouTrackClient.kt**
- Handles all YouTrack API interactions
- Supports fetching issues, creating issues, and custom field parsing
- Implements custom deserializer for flexible `CustomField` values (single or array)

**TelegramClient.kt**
- Manages Telegram Bot API communication
- Sends formatted notifications with Markdown
- Implements command parsing and response handling
- Uses long-polling to receive messages in real-time

**TeamsClient.kt**
- Manages Microsoft Teams Incoming Webhook communication
- Sends formatted notifications using MessageCard format
- Supports color-coded themes based on issue state
- Structures data as Facts for better readability

**Config.kt**
- Loads and validates environment variables from `.env`
- Provides type-safe configuration access
- Supports flexible messenger mode configuration

---

## 🎨 Messenger Modes

The application supports three operation modes via the `MESSENGER_MODE` environment variable:

### 1. Telegram Only (`MESSENGER_MODE=telegram`)
- ✅ Sends notifications to Telegram
- ✅ Telegram bot commands available
- ❌ Teams notifications disabled
- **Use case**: You only use Telegram or don't have Teams access

  

### 2. Teams Only (`MESSENGER_MODE=teams`)
- ❌ Telegram notifications disabled
- ❌ Telegram bot commands disabled
- ✅ Sends notifications to Teams
- **Use case**: You only use Teams or want to test Teams integration

![/teams](https://github.com/magosmihajlo/YouTrack-Integration-with-Microsoft-Teams-Internship/blob/818b71e00d7059b9c9d97c47c26bc16d1c9c773f/polling%20teams.png)

### 3. Both Messengers (`MESSENGER_MODE=both`)
- ✅ Sends notifications to **both** Telegram and Teams simultaneously
- ✅ Telegram bot commands available
- ✅ Parallel notification delivery for better performance
- **Use case**: Your team uses both platforms or you want maximum coverage

---

## 🐛 Troubleshooting

### Issue: "Missing YOUTRACK_TOKEN in .env"
**Solution:** Ensure your `.env` file exists in the project root and contains all required variables for your chosen `MESSENGER_MODE`.

### Issue: Telegram bot doesn't respond to commands
**Solution:**
- Verify the bot token is correct
- Ensure you've started a chat with the bot
- Confirm the chat ID matches your user/group ID
- Check that `MESSENGER_MODE` includes `telegram` or `both`

### Issue: "Failed to create issue in YouTrack"
**Solution:**
- Check that your token has "Create Issue" permissions
- Verify the project ID is correct
- Ensure the project exists and is accessible

### Issue: Teams notifications not appearing
**Solution:**
- Verify the webhook URL is correct and hasn't been deleted
- Test the webhook with curl: `curl -H "Content-Type: application/json" -d '{"text":"Test"}' YOUR_WEBHOOK_URL`
- Check that the connector is still active in Teams channel settings
- Ensure `MESSENGER_MODE` includes `teams` or `both`

### Issue: Notifications not appearing
**Solution:**
- Check application logs for errors
- Verify network connectivity to YouTrack and messenger APIs
- Try creating a test issue manually in YouTrack
- Confirm poll interval isn't too long (try reducing to 30 seconds for testing)

---

## 🎯 Implementation Notes

### Part 1: Notifications ✅ COMPLETED
- ✅ YouTrack → Telegram notifications
- ✅ YouTrack → Microsoft Teams notifications
- ✅ Multi-messenger support
- ✅ Parallel notification delivery
- ✅ Configurable polling
- ✅ Smart caching to prevent duplicates

### Part 2: Interactive Commands ⚠️ PARTIAL
- ✅ Telegram bot commands (create, list, info issues)
- ❌ Teams bot commands (requires Azure Bot Framework - significantly more complex)

**Why Teams commands aren't included:**
- Teams Incoming Webhooks are **one-way** (app → Teams only)
- Interactive Teams bots require:
    - Azure Bot Service registration
    - OAuth authentication setup
    - App manifest and deployment
    - Significantly more infrastructure
- For an internship task focused on notifications, Incoming Webhooks provide the best balance of simplicity and functionality

---

## 🚀 Future Enhancements

Potential improvements aligned with the internship goals:

- [ ] **Teams Bot Commands** — Full Azure Bot Service integration for bidirectional communication
- [ ] **Webhook Support** — Replace polling with real-time webhooks for instant notifications
- [ ] **Rich Formatting** — Add issue links, assignee info, and priority badges
- [ ] **Multi-Project Support** — Monitor multiple YouTrack projects simultaneously
- [ ] **Notification Filters** — Allow users to filter by priority, assignee, or labels
- [ ] **Database Persistence** — Store notification history and user preferences
- [ ] **Docker Support** — Containerize for easier deployment
- [ ] **Slack Integration** — Add Slack as a third messenger option
- [ ] **Dashboard** — Web UI for configuration and monitoring

---

## 📄 API References

- [YouTrack REST API Documentation](https://www.jetbrains.com/help/youtrack/devportal/api-overview.html)
- [Telegram Bot API Documentation](https://core.telegram.org/bots/api)
- [Microsoft Teams Incoming Webhooks](https://learn.microsoft.com/en-us/microsoftteams/platform/webhooks-and-connectors/how-to/add-incoming-webhook)
- [Teams MessageCard Reference](https://learn.microsoft.com/en-us/outlook/actionable-messages/message-card-reference)
- [Ktor Documentation](https://ktor.io/docs/welcome.html)

---

## 📝 License

This project is created for educational purposes as part of an internship application task.

---

## 👤 Author

Created with Kotlin as part of the YouTrack integration internship application.

**Technologies Demonstrated:**
- REST API integration (YouTrack, Telegram, Teams)
- Asynchronous programming with Kotlin Coroutines
- Multi-platform messenger support
- Webhook and Bot API implementation
- Robust error handling and logging
- Clean architecture and separation of concerns

---

## 🙏 Acknowledgments

- **JetBrains** for YouTrack and Kotlin
- **Telegram** for their excellent Bot API
- **Microsoft** for Teams Incoming Webhooks
- **Ktor team** for a fantastic HTTP client library
- **kotlinx.serialization** for type-safe JSON handling

---

---

**Ready to integrate YouTrack with your team's favorite messengers! 🚀**
