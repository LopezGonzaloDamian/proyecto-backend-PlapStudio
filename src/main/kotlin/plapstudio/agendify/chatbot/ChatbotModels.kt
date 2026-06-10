package plapstudio.agendify.chatbot

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request del chatbot de Agendify")
data class ChatbotRequest(
    @field:Schema(
        description = "Historial de mensajes de la conversacion",
        example = """[{"role":"user","content":"Como reservo un turno?"}]"""
    )
    val messages: List<ChatbotMessage> = emptyList(),
    @field:Schema(description = "Indica si el usuario esta autenticado", example = "false")
    val authenticated: Boolean = false,
    @field:Schema(
        description = "Rol declarado del usuario dentro de Agendify",
        example = "CLIENTE",
        nullable = true
    )
    val userRole: String? = null
)

@Schema(description = "Mensaje individual del chatbot")
data class ChatbotMessage(
    @field:Schema(description = "Rol del mensaje dentro de la conversacion", example = "user")
    val role: String,
    @field:Schema(description = "Contenido del mensaje", example = "Como funciona la sena?")
    val content: String
)

@Schema(description = "Respuesta del chatbot")
data class ChatbotResponse(
    @field:Schema(
        description = "Respuesta generada para el usuario",
        example = "Para reservar un turno primero busca un profesional y luego elige un horario disponible."
    )
    val message: String,
    @field:Schema(description = "Origen de la respuesta", example = "gemini")
    val source: String = "static-help"
)

data class GeminiGenerateContentRequest(
    val systemInstruction: GeminiContent,
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig = GeminiGenerationConfig()
)

data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiGenerationConfig(
    val temperature: Double = 0.35,
    val maxOutputTokens: Int = 280
)

data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

data class GeminiCandidate(
    val content: GeminiContent? = null
)
