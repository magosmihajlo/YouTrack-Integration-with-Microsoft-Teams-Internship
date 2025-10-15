package config

import io.github.cdimascio.dotenv.dotenv

data class Config(
    val youTrackBaseUrl: String,
    val youTrackToken: String,
    val youTrackProjectId: String,
    val telegramApiBaseUrl: String,
    val telegramBotToken: String,
    val telegramChatId: String,
    val teamsWebhookUrl: String?,
    val messengerMode: String,
    val pollIntervalSeconds: Int
) {
    companion object {
        private const val DEFAULT_TELEGRAM_API_URL = "https://api.telegram.org"
        private const val DEFAULT_POLL_INTERVAL = 60
        private const val MIN_POLL_INTERVAL = 10
        private const val MAX_POLL_INTERVAL = 3600

        private val VALID_MESSENGER_MODES = setOf("telegram", "teams", "both")

        fun load(): Config {
            val dotenv = dotenv {
                directory = "./"
                ignoreIfMissing = false
            }

            val youTrackUrl = dotenv["YOUTRACK_URL"]?.trimEnd('/')
                ?.also { require(it.startsWith("http")) { "YOUTRACK_URL must start with http:// or https://" } }
                ?: error("Missing YOUTRACK_URL in .env")

            val youTrackToken = dotenv["YOUTRACK_TOKEN"]?.trim()
                ?.also { require(it.isNotBlank()) { "YOUTRACK_TOKEN cannot be blank" } }
                ?: error("Missing YOUTRACK_TOKEN in .env")

            val projectId = dotenv["YOUTRACK_PROJECT_ID"]?.trim()
                ?.also { require(it.isNotBlank()) { "YOUTRACK_PROJECT_ID cannot be blank" } }
                ?: error("Missing YOUTRACK_PROJECT_ID in .env")

            val messengerMode = dotenv["MESSENGER_MODE"]?.lowercase()?.trim() ?: "both"
            require(messengerMode in VALID_MESSENGER_MODES) {
                "MESSENGER_MODE must be one of: ${VALID_MESSENGER_MODES.joinToString()}"
            }

            val telegramApiUrl = dotenv["TELEGRAM_API_URL"]?.trimEnd('/') ?: DEFAULT_TELEGRAM_API_URL

            val telegramToken = if (messengerMode in listOf("telegram", "both")) {
                dotenv["TELEGRAM_BOT_TOKEN"]?.trim()
                    ?.also { require(it.contains(":")) { "TELEGRAM_BOT_TOKEN appears invalid" } }
                    ?: error("TELEGRAM_BOT_TOKEN is required when MESSENGER_MODE is '$messengerMode'")
            } else {
                dotenv["TELEGRAM_BOT_TOKEN"] ?: "dummy-token"
            }

            val telegramChatId = if (messengerMode in listOf("telegram", "both")) {
                dotenv["TELEGRAM_CHAT_ID"]?.trim()
                    ?.also { require(it.isNotBlank()) { "TELEGRAM_CHAT_ID cannot be blank" } }
                    ?: error("TELEGRAM_CHAT_ID is required when MESSENGER_MODE is '$messengerMode'")
            } else {
                dotenv["TELEGRAM_CHAT_ID"] ?: "0"
            }

            val teamsWebhook = if (messengerMode in listOf("teams", "both")) {
                dotenv["TEAMS_WEBHOOK_URL"]?.trim()
                    ?.also {
                        require(it.isNotBlank()) { "TEAMS_WEBHOOK_URL cannot be blank" }
                        require(it.startsWith("https://")) { "TEAMS_WEBHOOK_URL must be a valid HTTPS URL" }
                    }
                    ?: error("TEAMS_WEBHOOK_URL is required when MESSENGER_MODE is '$messengerMode'")
            } else {
                dotenv["TEAMS_WEBHOOK_URL"]?.trim()?.takeIf { it.isNotBlank() }
            }

            val pollInterval = dotenv["POLL_INTERVAL_SECONDS"]?.toIntOrNull() ?: DEFAULT_POLL_INTERVAL
            require(pollInterval in MIN_POLL_INTERVAL..MAX_POLL_INTERVAL) {
                "POLL_INTERVAL_SECONDS must be between $MIN_POLL_INTERVAL and $MAX_POLL_INTERVAL"
            }

            return Config(
                youTrackBaseUrl = youTrackUrl,
                youTrackToken = youTrackToken,
                youTrackProjectId = projectId,
                telegramApiBaseUrl = telegramApiUrl,
                telegramBotToken = telegramToken,
                telegramChatId = telegramChatId,
                teamsWebhookUrl = teamsWebhook,
                messengerMode = messengerMode,
                pollIntervalSeconds = pollInterval
            )
        }
    }
}