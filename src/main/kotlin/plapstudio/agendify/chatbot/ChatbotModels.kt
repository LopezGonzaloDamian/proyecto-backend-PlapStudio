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
        description = "Campo de compatibilidad. Si hay token valido, el backend resuelve la autenticacion real desde la sesion.",
        example = "false"
    )
    val authenticated: Boolean = false,
    @field:Schema(
        description = "Campo de compatibilidad. Si hay token valido, el backend resuelve el rol real desde el usuario autenticado.",
        example = "CLIENTE",
        nullable = true
    )
    val userRole: String? = null,
    @field:Schema(description = "Accion opcional para navegar por menu o abrir opciones guiadas", nullable = true)
    val action: ChatbotActionRequest? = null
)

@Schema(description = "Mensaje individual del chatbot")
data class ChatbotMessage(
    @field:Schema(description = "Rol del mensaje dentro de la conversacion", example = "user")
    val role: String,
    @field:Schema(description = "Contenido del mensaje", example = "Como funciona la sena?")
    val content: String
)

@Schema(description = "Accion enviada por el frontend para navegar el menu del chatbot")
data class ChatbotActionRequest(
    @field:Schema(description = "Tipo de accion", example = "OPEN_NODE")
    val type: ChatbotOptionType,
    @field:Schema(description = "Nodo de destino cuando aplica", example = "payments_menu", nullable = true)
    val targetNodeId: String? = null,
    @field:Schema(description = "Payload opcional cuando aplica", example = "Como reservar un turno", nullable = true)
    val payload: String? = null
)

enum class ChatbotOptionType {
    OPEN_NODE,
    BACK,
    MAIN_MENU,
    TEXT_INTENT
}

@Schema(description = "Opcion accionable que muestra el chatbot")
data class ChatbotOption(
    @field:Schema(description = "Identificador unico de la opcion", example = "payments_menu")
    val id: String,
    @field:Schema(description = "Texto visible del boton", example = "Pagos y senas")
    val label: String,
    @field:Schema(description = "Tipo de accion de la opcion", example = "OPEN_NODE")
    val type: ChatbotOptionType,
    @field:Schema(description = "Nodo destino cuando aplica", example = "payments_menu", nullable = true)
    val targetNodeId: String? = null,
    @field:Schema(description = "Payload de texto opcional", example = "Como reservar un turno", nullable = true)
    val payload: String? = null
)

@Schema(description = "Respuesta del chatbot")
data class ChatbotResponse(
    @field:Schema(
        description = "Respuesta generada para el usuario",
        example = "Para reservar un turno primero busca un profesional y luego elige un horario disponible."
    )
    val message: String,
    @field:Schema(description = "Origen de la respuesta", example = "menu")
    val source: String = "fallback",
    @field:Schema(description = "Opciones principales para seguir navegando")
    val options: List<ChatbotOption> = emptyList(),
    @field:Schema(description = "Nodo actual de la conversacion guiada", example = "reserve_menu", nullable = true)
    val currentNodeId: String? = null,
    @field:Schema(description = "Nodo anterior si existe", example = "main_menu", nullable = true)
    val previousNodeId: String? = null,
    @field:Schema(description = "Opciones sugeridas relacionadas con la respuesta")
    val suggestedActions: List<ChatbotOption> = emptyList()
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
    val temperature: Double = 0.3,
    val maxOutputTokens: Int = 260
)

data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate> = emptyList()
)

data class GeminiCandidate(
    val content: GeminiContent? = null
)
