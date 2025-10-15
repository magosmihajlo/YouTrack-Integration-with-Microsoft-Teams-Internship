import config.Config
import telegram.TelegramClient
import youtrack.YouTrackClient
import kotlinx.coroutines.*
import java.time.Instant
import org.slf4j.LoggerFactory


suspend fun runNotifier() {
    val config = Config.load()
    val youTrack = YouTrackClient(config)
    val telegram = TelegramClient(config)
    val logger = LoggerFactory.getLogger("MainKt")

    val seenIssues = mutableSetOf<String>()

    logger.info("🚀 YouTrack → Telegram notifier started!")
    logger.info("📡 Polling every ${config.pollIntervalSeconds} seconds...")

    while (true) {
        try {
            if (seenIssues.size > 1000) {
                logger.info("Cache size > 1000, trimming to the 500 most recent issues.")
                val recentIssues = seenIssues.toList().takeLast(500).toMutableSet()
                seenIssues.clear()
                seenIssues.addAll(recentIssues)
            }

            val issues = youTrack.fetchIssues()

            for (issue in issues) {
                if (issue.idReadable !in seenIssues) {
                    val message = $$"""
                        🆕 *New YouTrack Issue*
                        *ID:* $${issue.idReadable}
                        *Title:* $${issue.summary}
                        *State:* $${issue.state ?: "_unknown_"}
                        *Description:* $${issue.description?.take(500) ?: "_(no description)_"}
                        ⏰ $${Instant.now()}
                    """.trimIndent()

                    telegram.sendMessage(message)
                    logger.info("✅ Sent notification for ${issue.idReadable}")

                    seenIssues += issue.idReadable
                }
            }
        } catch (e: Exception) {
            logger.info("❌ Error: ${e.message}")
        }

        delay(config.pollIntervalSeconds * 1000L)
    }
}

fun main() = runBlocking {
    val config = Config.load()
    val youTrack = YouTrackClient(config)
    val telegram = TelegramClient(config)

    val jobNotifier = launch { runNotifier() }
    val jobTelegram = launch { telegram.pollMessages(youTrack) }

    joinAll(jobNotifier, jobTelegram)
}

