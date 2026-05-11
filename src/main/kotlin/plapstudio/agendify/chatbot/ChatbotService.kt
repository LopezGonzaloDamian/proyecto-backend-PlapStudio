package plapstudio.agendify.chatbot

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import plapstudio.agendify.errors.BusinessException

@Service
class ChatbotService(
    private val chatbotKnowledgeBase: ChatbotKnowledgeBase,
    private val chatbotPromptFactory: ChatbotPromptFactory,
    private val geminiChatClient: GeminiChatClient,
    @Value("\${agendify.chatbot.gemini.enabled:true}") private val geminiEnabled: Boolean,
    @Value("\${agendify.chatbot.gemini.api-key:}") private val geminiApiKey: String,
    @Value("\${agendify.chatbot.gemini.model:gemini-2.0-flash}") private val geminiModel: String
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

        if (!geminiEnabled || geminiApiKey.isBlank()) {
            return ChatbotResponse(
                message = staticReply,
                source = "static-help-fallback"
            )
        }

        val geminiReply = runCatching {
            geminiChatClient.generate(
                model = geminiModel,
                request = GeminiGenerateContentRequest(
                    systemInstruction = GeminiContent(parts = listOf(GeminiPart(chatbotPromptFactory.buildPrompt(sanitizedRequest)))),
                    contents = sanitizedRequest.messages.map { message ->
                        GeminiContent(
                            role = if (message.role.equals("assistant", ignoreCase = true)) "model" else "user",
                            parts = listOf(GeminiPart(message.content))
                        )
                    }
                )
            )
        }.getOrNull()

        if (!geminiReply.isNullOrBlank()) {
            return ChatbotResponse(
                message = geminiReply,
                source = "gemini"
            )
        }

        return ChatbotResponse(
            message = staticReply,
            source = "static-help-fallback"
        )
    }
}
