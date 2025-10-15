import config.Config
import telegram.TelegramClient
import teams.TeamsClient
import youtrack.YouTrackClient
import kotlinx.coroutines.*
import java.time.Instant
import org.slf4j.LoggerFactory

suspend fun runNotifier() {
    val config = Config.load()
    val youTrack = YouTrackClient(config)
    val telegram = TelegramClient(config)
    val teams = TeamsClient(config)
    val logger = LoggerFactory.getLogger("Notifier")

    val seenIssues = mutableSetOf<String>()

    val useTelegram = config.messengerMode in listOf("telegram", "both")
    val useTeams = config.messengerMode in listOf("teams", "both") && !config.teamsWebhookUrl.isNullOrBlank()

    logger.info("🚀 YouTrack Notifier started!")
    logger.info("📡 Polling every ${config.pollIntervalSeconds} seconds...")
    logger.info("📨 Active messengers: ${buildString {
        if (useTelegram) append("Telegram ")
        if (useTeams) append("Teams")
    }.ifBlank { "None" }}")

    while (true) {
        try {
            if (seenIssues.size > 1000) {
                logger.info("🧹 Trimming cache from ${seenIssues.size} to 500 entries")
                val recentIssues = seenIssues.toList().takeLast(500).toMutableSet()
                seenIssues.clear()
                seenIssues.addAll(recentIssues)
            }

            val issues = youTrack.fetchIssues()
            val newIssues = issues.filter { it.idReadable !in seenIssues }

            if (newIssues.isEmpty()) {
                logger.debug("No new issues found")
            }

            for (issue in newIssues) {
                val timestamp = Instant.now().toString()

                coroutineScope {
                    if (useTelegram) {
                        launch {
                            try {
                                val telegramMessage = """
                                    🆕 *New YouTrack Issue*
                                    *ID:* ${issue.idReadable}
                                    *Title:* ${issue.summary}
                                    *State:* ${issue.state ?: "_unknown_"}
                                    *Description:* ${issue.description?.take(500) ?: "_(no description)_"}
                                    ⏰ $timestamp
                                """.trimIndent()

                                telegram.sendMessage(telegramMessage)
                                logger.info("✅ [Telegram] ${issue.idReadable}")
                            } catch (e: Exception) {
                                logger.error("❌ [Telegram] Failed for ${issue.idReadable}: ${e.message}")
                            }
                        }
                    }

                    if (useTeams) {
                        launch {
                            try {
                                teams.sendNotification(
                                    issueId = issue.idReadable,
                                    title = issue.summary,
                                    state = issue.state,
                                    description = issue.description?.take(500),
                                    timestamp = timestamp
                                )
                                logger.info("✅ [Teams] ${issue.idReadable}")
                            } catch (e: Exception) {
                                logger.error("❌ [Teams] Failed for ${issue.idReadable}: ${e.message}")
                            }
                        }
                    }
                }

                seenIssues.add(issue.idReadable)
            }
        } catch (e: Exception) {
            logger.error("❌ Error in notification loop: ${e.message}", e)
        }

        delay(config.pollIntervalSeconds * 1000L)
    }
}

fun main() = runBlocking {
    val config = Config.load()
    val youTrack = YouTrackClient(config)
    val telegram = TelegramClient(config)
    val logger = LoggerFactory.getLogger("Main")

    val useTelegram = config.messengerMode in listOf("telegram", "both")

    val jobNotifier = launch { runNotifier() }
    val jobTelegram = if (useTelegram) {
        launch { telegram.pollMessages(youTrack) }
    } else {
        logger.info("⏭️ Telegram commands disabled (mode: ${config.messengerMode})")
        null
    }

    if (jobTelegram != null) {
        joinAll(jobNotifier, jobTelegram)
    } else {
        jobNotifier.join()
    }
}