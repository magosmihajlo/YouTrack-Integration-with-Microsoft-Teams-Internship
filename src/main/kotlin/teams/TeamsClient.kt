package teams

import config.Config
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class TeamsClient(private val config: Config) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
            socketTimeoutMillis = 30_000
        }
    }

    @Serializable
    data class TeamsMessageCard(
        @SerialName("@type")
        val type: String = "MessageCard",
        @SerialName("@context")
        val context: String = "https://schema.org/extensions",
        val summary: String,
        val themeColor: String = "0078D7", // Microsoft blue
        val title: String,
        val sections: List<Section>
    )

    @Serializable
    data class Section(
        val activityTitle: String? = null,
        val activitySubtitle: String? = null,
        val facts: List<Fact>? = null,
        val text: String? = null
    )

    @Serializable
    data class Fact(
        val name: String,
        val value: String
    )

    suspend fun sendNotification(
        issueId: String,
        title: String,
        state: String?,
        description: String?,
        timestamp: String
    ) {
        val webhookUrl = config.teamsWebhookUrl
        if (webhookUrl.isNullOrBlank()) {
            logger.warn("⚠️ Teams webhook URL not configured. Skipping Teams notification.")
            return
        }

        val messageCard = createMessageCard(issueId, title, state, description, timestamp)

        try {
            val response: HttpResponse = client.post(webhookUrl) {
                contentType(ContentType.Application.Json)
                setBody(messageCard)
            }

            if (response.status.isSuccess()) {
                logger.info("✅ Sent Teams notification for $issueId")
            } else {
                val errorBody = response.bodyAsText()
                logger.error("❌ Failed to send Teams notification: ${response.status} - $errorBody")
            }
        } catch (e: Exception) {
            logger.error("❌ Exception while sending Teams notification for $issueId", e)
        }
    }

    private fun createMessageCard(
        issueId: String,
        title: String,
        state: String?,
        description: String?,
        timestamp: String
    ): TeamsMessageCard {
        return TeamsMessageCard(
            summary = "New YouTrack Issue: $issueId",
            title = "🆕 New YouTrack Issue",
            themeColor = when {
                state?.contains("open", ignoreCase = true) == true -> "FFA500" // Orange
                state?.contains("resolved", ignoreCase = true) == true -> "28A745" // Green
                state?.contains("closed", ignoreCase = true) == true -> "6C757D" // Gray
                else -> "0078D7" // Blue
            },
            sections = listOf(
                Section(
                    activityTitle = title,
                    activitySubtitle = issueId,
                    facts = listOf(
                        Fact("State", state ?: "Unknown"),
                        Fact("Created", timestamp)
                    ),
                    text = description ?: "_No description provided_"
                )
            )
        )
    }

    @Serializable
    data class SimpleTeamsMessage(
        val text: String
    )

    suspend fun sendSimpleMessage(text: String) {
        val webhookUrl = config.teamsWebhookUrl
        if (webhookUrl.isNullOrBlank()) {
            logger.warn("⚠️ Teams webhook URL not configured.")
            return
        }

        val message = SimpleTeamsMessage(text = text)

        try {
            val response: HttpResponse = client.post(webhookUrl) {
                contentType(ContentType.Application.Json)
                setBody(message)
            }

            if (response.status.isSuccess()) {
                logger.info("✅ Sent simple Teams message")
            } else {
                logger.error("❌ Failed to send Teams message: ${response.status} - ${response.bodyAsText()}")
            }
        } catch (e: Exception) {
            logger.error("❌ Exception sending Teams message", e)
        }
    }

    fun close() {
        client.close()
    }
}