package plapstudio.agendify.chatbot

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import plapstudio.agendify.errors.BusinessException

@Service
class ChatbotService(
    private val chatbotKnowledgeBase: ChatbotKnowledgeBase,
    private val chatbotPromptFactory: ChatbotPromptFactory,
    private val ollamaChatClient: OllamaChatClient,
    @Value("\${agendify.chatbot.ollama.enabled:true}") private val ollamaEnabled: Boolean,
    @Value("\${agendify.chatbot.ollama.model:llama3.2:3b}") private val ollamaModel: String
) {

    fun reply(request: ChatbotRequest): ChatbotResponse {
        val sanitizedMessages = request.messages
            .map { ChatbotMessage(role = it.role.trim(), content = it.content.trim()) }
            .filter { it.content.isNotBlank() }

        if (sanitizedMessages.isEmpty()) {
            throw BusinessException("El chatbot necesita al menos un mensaje para responder")
        }

        val sanitizedRequest = request.copy(messages = sanitizedMessages)
        val staticReply = chatbotKnowledgeBase.buildReply(sanitizedRequest)

        if (!ollamaEnabled) {
            return ChatbotResponse(message = staticReply)
        }

        val ollamaReply = runCatching {
            val messages = chatbotPromptFactory.buildMessages(sanitizedRequest)
            ollamaChatClient.chat(
                OllamaChatRequest(
                    model = ollamaModel,
                    messages = messages
                )
            )
        }.getOrNull()

        if (!ollamaReply.isNullOrBlank()) {
            return ChatbotResponse(
                message = ollamaReply,
                source = "ollama"
            )
        }

        return ChatbotResponse(
            message = staticReply,
            source = "static-help-fallback"
        )
    }
}
