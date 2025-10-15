package telegram

import config.Config
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.slf4j.LoggerFactory

class TelegramClient(private val config: Config) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private val baseBotUrl = "${config.telegramApiBaseUrl}/bot${config.telegramBotToken}"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    @Serializable
    data class SendMessageRequest(
        val chatId: String,
        val text: String,
        val parseMode: String = "Markdown"
    )

    suspend fun sendMessage(text: String) {
        val url = "$baseBotUrl/sendMessage"
        val requestBody = SendMessageRequest(chatId = config.telegramChatId, text = text)

        try {
            val response: HttpResponse = client.post(url) {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
            if (!response.status.isSuccess()) {
                logger.error("Failed to send message to Telegram: ${response.status} - ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            logger.error("Exception while sending message to Telegram", e)
        }
    }

    suspend fun handleMessage(text: String, youTrack: youtrack.YouTrackClient) {
        when {
            text.startsWith("/new") -> {
                val regex = Regex("""/new\s+"([^"]+)"\s+"([^"]+)"""")
                val match = regex.find(text)

                if (match != null) {
                    val (title, description) = match.destructured
                    sendMessage("📝 Creating new issue: *$title* ...")

                    try {
                        val issue = youTrack.createIssue(title, description)
                        if (issue != null) {
                            sendMessage("✅ Issue *${issue.idReadable}* created!\nTitle: ${issue.summary}")
                        } else {
                            sendMessage("⚠️ Failed to create issue in YouTrack.")
                        }
                    } catch (e: Exception) {
                        sendMessage("❌ Error while creating issue: ${e.message}")
                    }
                } else {
                    sendMessage("⚠️ Wrong format. Use:\n`/new \"Title\" \"Description\"`")
                }
            }

            text.startsWith("/list") -> {
                try {
                    val issues = youTrack.fetchIssues(limit = 5)
                    if (issues.isEmpty()) {
                        sendMessage("📭 No issues found.")
                    } else {
                        val message = issues.joinToString("\n\n") { issue ->
                            "*${issue.idReadable}* — ${issue.summary}\n_${issue.state ?: "unknown"}_"
                        }
                        sendMessage("*🗂️ Latest Issues:*\n\n$message")
                    }
                } catch (e: Exception) {
                    sendMessage("❌ Error fetching issues: ${e.message}")
                }
            }

            text.startsWith("/info") -> {
                val regex = Regex("""/info\s+(\S+)""")
                val match = regex.find(text)

                if (match != null) {
                    val issueId = match.groupValues[1]
                    try {
                        val issue = youTrack.fetchIssue(issueId)
                        if (issue != null) {
                            val message = $$"""
                            🧾 *Issue Details*
                            *ID:* $${issue.idReadable}
                            *Title:* $${issue.summary}
                            *State:* $${issue.state ?: "_unknown_"}
                            *Description:* $${issue.description ?: "_(no description)_"}
                        """.trimIndent()
                            sendMessage(message)
                        } else {
                            sendMessage("⚠️ Issue $issueId not found.")
                        }
                    } catch (e: Exception) {
                        sendMessage("❌ Error fetching issue details: ${e.message}")
                    }
                } else {
                    sendMessage("⚠️ Use: `/info ISSUE_ID` (e.g., `/info MI-13`)")
                }
            }

            text.startsWith("/help") -> {
                val helpMessage = """
                🤖 *Available Commands:*
                `/new "Title" "Description"` — Create new issue in YouTrack  
                `/list` — Show latest 5 issues  
                `/info ISSUE_ID` — Show detailed info for one issue  
                `/help` — Show this help message
            """.trimIndent()
                sendMessage(helpMessage)
            }

            else -> {
                sendMessage("🤖 Unknown command. Try `/help` for available commands.")
            }
        }
    }


    suspend fun pollMessages(youTrack: youtrack.YouTrackClient) {
        var offset = 0L
        val urlBase = baseBotUrl

        while (true) {
            try {
                val response: HttpResponse = client.get("$urlBase/getUpdates") {
                    parameter("offset", offset)
                    parameter("timeout", 10)
                }

                val body: String = response.body()
                val updates = Json.parseToJsonElement(body)
                    .jsonObject["result"]?.jsonArray ?: continue

                for (update in updates) {
                    val obj = update.jsonObject
                    val message = obj["message"]?.jsonObject
                    val text = message?.get("text")?.jsonPrimitive?.content
                    val chatId = message?.get("chat")?.jsonObject?.get("id")?.jsonPrimitive?.long

                    if (text != null && chatId != null && chatId.toString() == config.telegramChatId) {
                        handleMessage(text, youTrack)
                    }

                    offset = obj["update_id"]?.jsonPrimitive?.long?.plus(1) ?: offset
                }
            } catch (e: Exception) {
                logger.error("❌ Telegram polling error: ${e.message}")
            }

            delay(1000L)
        }
    }

    fun close() {
        client.close()
    }
}
