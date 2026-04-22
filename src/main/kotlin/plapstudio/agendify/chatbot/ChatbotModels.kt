package plapstudio.agendify.chatbot

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request del chatbot de Agendify")
data class ChatbotRequest(
    @field:Schema(
        description = "Historial de mensajes de la conversacion",
        example = """[{"role":"user","content":"Como reservo un turno?"}]"""
    )
    val messages: List<ChatbotMessage> = emptyList(),
    @field:Schema(
        description = "Indica si el usuario esta autenticado",
        example = "false"
    )
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
    @field:Schema(
        description = "Rol del mensaje dentro de la conversacion",
        example = "user"
    )
    val role: String,
    @field:Schema(
        description = "Contenido del mensaje",
        example = "Como funciona la seña?"
    )
    val content: String
)

@Schema(description = "Respuesta del chatbot")
data class ChatbotResponse(
    @field:Schema(
        description = "Respuesta generada para el usuario",
        example = "En Agendify la reserva se realiza buscando un profesional, revisando disponibilidad y eligiendo un turno."
    )
    val message: String,
    @field:Schema(
        description = "Origen de la respuesta",
        example = "ollama"
    )
    val source: String = "static-help"
)

data class OllamaChatRequest(
    val model: String,
    val stream: Boolean = false,
    val messages: List<ChatbotMessage>,
    val options: Map<String, Any> = mapOf(
        "temperature" to 0.35,
        "num_predict" to 280
    )
)

data class OllamaChatResponse(
    val message: OllamaMessage? = null
)

data class OllamaMessage(
    val content: String? = null
)
