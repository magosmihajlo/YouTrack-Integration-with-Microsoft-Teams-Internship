package youtrack

import config.Config
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.JsonDecoder

class YouTrackClient(private val config: Config) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            })
        }
    }

    @Serializable
    data class ProjectId(val id: String)

    @Serializable
    data class CreateIssueRequest(val project: ProjectId, val summary: String, val description: String)

    @Serializable
    data class Issue(
        val idReadable: String,
        val summary: String,
        val description: String? = null,
        val customFields: List<CustomField>? = null
    ) {
        val state: String?
            get() = customFields
                ?.firstOrNull { it.name.equals("State", ignoreCase = true) || it.name.equals("Status", ignoreCase = true) }
                ?.valueName
    }

    @Serializable
    data class CustomField(
        val name: String,
        val value: FieldValueWrapper? = null
    ) {
        val valueName: String?
            get() = value?.asSingle()?.name
    }

    @Serializable
    data class FieldValue(val name: String? = null)

    @Serializable(with = FieldValueWrapperSerializer::class)
    data class FieldValueWrapper(val single: FieldValue? = null, val list: List<FieldValue>? = null) {
        fun asSingle(): FieldValue? = single ?: list?.firstOrNull()
    }

    object FieldValueWrapperSerializer : KSerializer<FieldValueWrapper> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("FieldValueWrapper")

        override fun deserialize(decoder: Decoder): FieldValueWrapper {
            val input = decoder as? JsonDecoder ?: error("FieldValueWrapper can be deserialized only by JSON")
            val element = input.decodeJsonElement()

            return when {
                element is kotlinx.serialization.json.JsonObject -> FieldValueWrapper(single = input.json.decodeFromJsonElement(FieldValue.serializer(), element))
                element is kotlinx.serialization.json.JsonArray -> FieldValueWrapper(list = input.json.decodeFromJsonElement(ListSerializer(FieldValue.serializer()), element))
                else -> FieldValueWrapper()
            }
        }

        override fun serialize(encoder: Encoder, value: FieldValueWrapper) {
            error("Serialization not supported")
        }
    }


    suspend fun fetchIssues(limit: Int = 10): List<Issue> {
        val url = "${config.youTrackBaseUrl}/api/issues?fields=id,idReadable,summary,description,customFields(name,value(name))&\$top=$limit"
        return client.get(url) {
            header("Authorization", "Bearer ${config.youTrackToken}")
            header("Accept", "application/json")
        }.body()
    }

    suspend fun fetchIssue(issueId: String): Issue? {
        val url = "${config.youTrackBaseUrl}/api/issues/$issueId?fields=id,idReadable,summary,description,customFields(name,value(name))"
        return try {
            client.get(url) {
                header("Authorization", "Bearer ${config.youTrackToken}")
                header("Accept", "application/json")
            }.body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createIssue(title: String, description: String): Issue? {
        val url = "${config.youTrackBaseUrl}/api/issues?fields=idReadable,summary"

        val requestBody = CreateIssueRequest(
            project = ProjectId(config.youTrackProjectId),
            summary = title,
            description = description
        )

        return try {
            val response = client.post(url) {
                header(HttpHeaders.Authorization, "Bearer ${config.youTrackToken}")
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (!response.status.isSuccess()) {
                logger.error("⚠️ Failed to create issue: ${response.status} - ${response.bodyAsText()}")
                null
            } else {
                response.body()
            }
        } catch (e: Exception) {
            logger.error("❌ Exception while creating issue", e)
            null
        }
    }

    fun close() {
        client.close()
    }
}
