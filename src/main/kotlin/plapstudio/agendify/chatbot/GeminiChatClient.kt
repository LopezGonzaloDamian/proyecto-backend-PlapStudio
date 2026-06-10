package plapstudio.agendify.chatbot

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class GeminiChatClient(
    @Value("\${agendify.chatbot.gemini.base-url:https://generativelanguage.googleapis.com}") private val baseUrl: String,
    @Value("\${agendify.chatbot.gemini.api-key:}") private val apiKey: String
) {

    private val restClient: RestClient = RestClient.builder()
        .baseUrl(baseUrl)
        .build()

    fun generate(model: String, request: GeminiGenerateContentRequest): String {
        val response = restClient.post()
            .uri("/v1beta/models/$model:generateContent")
            .contentType(MediaType.APPLICATION_JSON)
            .header("x-goog-api-key", apiKey)
            .body(request)
            .retrieve()
            .body(GeminiGenerateContentResponse::class.java)

        return response?.candidates
            ?.firstOrNull()
            ?.content
            ?.parts
            ?.joinToString("\n") { it.text.trim() }
            ?.trim()
            .orEmpty()
    }
}
