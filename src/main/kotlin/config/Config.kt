package config

import io.github.cdimascio.dotenv.dotenv

data class Config(
    val youTrackBaseUrl: String,
    val youTrackToken: String,
    val youTrackProjectId: String,
    val telegramApiBaseUrl: String,
    val telegramBotToken: String,
    val telegramChatId: String,
    val pollIntervalSeconds: Int = 60
) {
    companion object {
        fun load(): Config {
            val dotenv = dotenv {
                directory = "./"
                ignoreIfMissing = false
            }

            return Config(
                youTrackBaseUrl = dotenv["YOUTRACK_URL"]?.trimEnd('/')
                    ?: error("Missing YOUTRACK_URL in .env"),
                youTrackToken = dotenv["YOUTRACK_TOKEN"]
                    ?: error("Missing YOUTRACK_TOKEN in .env"),
                youTrackProjectId = dotenv["YOUTRACK_PROJECT_ID"]
                    ?: error("Missing YOUTRACK_PROJECT_ID in .env"),
                telegramApiBaseUrl = dotenv["TELEGRAM_API_URL"]?.trimEnd('/')
                    ?: "https://api.telegram.org",
                telegramBotToken = dotenv["TELEGRAM_BOT_TOKEN"]
                    ?: error("Missing TELEGRAM_BOT_TOKEN in .env"),
                telegramChatId = dotenv["TELEGRAM_CHAT_ID"]
                    ?: error("Missing TELEGRAM_CHAT_ID in .env"),
                pollIntervalSeconds = dotenv["POLL_INTERVAL_SECONDS"]?.toIntOrNull() ?: 60
            )
        }
    }
}

