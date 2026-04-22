package plapstudio.agendify.chatbot

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class OllamaChatClient(
    @Value("\${agendify.chatbot.ollama.base-url:http://127.0.0.1:11434}") private val ollamaBaseUrl: String
) {

    private val restClient: RestClient = RestClient.builder()
        .baseUrl(ollamaBaseUrl)
        .build()

    fun chat(request: OllamaChatRequest): String {
        val response = restClient.post()
            .uri("/api/chat")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(OllamaChatResponse::class.java)

        return response?.message?.content?.trim().orEmpty()
    }
}
