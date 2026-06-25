package plapstudio.agendify.chatbot

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import plapstudio.agendify.auth.AuthContext
import plapstudio.agendify.domain.Usuario
import plapstudio.agendify.errors.BusinessException
import plapstudio.agendify.repository.UsuarioRepository

@Service
class ChatbotService(
    private val authContext: AuthContext,
    private val usuarioRepository: UsuarioRepository,
    private val chatbotKnowledgeBase: ChatbotKnowledgeBase,
    private val chatbotPromptFactory: ChatbotPromptFactory,
    private val geminiChatClient: GeminiChatClient,
    @Value("\${agendify.chatbot.gemini.enabled:true}") private val geminiEnabled: Boolean,
    @Value("\${agendify.chatbot.gemini.api-key:}") private val geminiApiKey: String,
    @Value("\${agendify.chatbot.gemini.model:gemini-2.0-flash}") private val geminiModel: String
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun reply(request: ChatbotRequest): ChatbotResponse {
        val sanitizedMessages = request.messages
            .map { ChatbotMessage(role = it.role.trim(), content = it.content.trim()) }
            .filter { it.content.isNotBlank() }
            .takeLast(12)

        if (sanitizedMessages.isEmpty() && request.action == null) {
            throw BusinessException("El chatbot necesita un mensaje o una accion para responder")
        }

        val resolvedRequest = resolveContext(request.copy(messages = sanitizedMessages))
        val draft = chatbotKnowledgeBase.buildReply(resolvedRequest)

        if (chatbotKnowledgeBase.shouldBypassModel(draft)) {
            return draft.toResponse()
        }

        if (!geminiEnabled || geminiApiKey.isBlank() || !draft.allowHumanization) {
            return draft.toResponse(source = if (draft.source == "intent") "intent" else draft.source)
        }

        val geminiReply = runCatching {
            geminiChatClient.generate(
                model = geminiModel,
                request = GeminiGenerateContentRequest(
                    systemInstruction = GeminiContent(
                        parts = listOf(
                            GeminiPart(
                                chatbotPromptFactory.buildPrompt(
                                    request = resolvedRequest,
                                    factualGuide = draft.factualGuide
                                )
                            )
                        )
                    ),
                    contents = resolvedRequest.messages.map { message ->
                        GeminiContent(
                            role = if (message.role.equals("assistant", ignoreCase = true)) "model" else "user",
                            parts = listOf(GeminiPart(message.content))
                        )
                    }
                )
            )
        }.onFailure { error ->
            logger.warn("Gemini no pudo responder y se usa fallback local: {}", error.message)
        }.getOrNull()

        if (!geminiReply.isNullOrBlank()) {
            return draft.toResponse(message = geminiReply, source = "gemini")
        }

        return draft.toResponse(source = draft.source)
    }

    private fun resolveContext(request: ChatbotRequest): ChatbotRequest {
        val principal = authContext.get() ?: return request.copy(authenticated = false, userRole = null)
        val usuario = usuarioRepository.findByIdOrNull(principal.userId)
            ?.takeIf(Usuario::activo)
            ?: return request.copy(authenticated = false, userRole = null)

        return request.copy(
            authenticated = true,
            userRole = resolveUserRole(usuario, request.userRole)
        )
    }

    private fun resolveUserRole(usuario: Usuario, requestedRole: String?): String? {
        val normalizedRequestedRole = requestedRole
            ?.trim()
            ?.uppercase()
            ?.takeIf { usuario.tieneRol(it) }

        if (normalizedRequestedRole != null) {
            return normalizedRequestedRole
        }

        return when {
            usuario.requiereSeleccionRol() -> "SIN_DEFINIR"
            usuario.esProfesional() -> "PROFESIONAL"
            usuario.esAsistente() -> "ASISTENTE"
            usuario.esCliente() -> "CLIENTE"
            usuario.esAdmin() -> "ADMIN"
            else -> null
        }
    }

    private fun ChatbotReplyDraft.toResponse(
        message: String = this.message,
        source: String = this.source
    ): ChatbotResponse = ChatbotResponse(
        message = message,
        source = source,
        options = options,
        currentNodeId = currentNodeId,
        previousNodeId = previousNodeId,
        suggestedActions = suggestedActions
    )
}
