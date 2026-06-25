package plapstudio.agendify.chatbot

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(
    name = "Chatbot",
    description = "Asistente virtual de Agendify para consultas, guias y ayuda sobre el producto"
)
@RestController
@RequestMapping("/chatbot")
@CrossOrigin("*")
class ChatbotController(
    private val chatbotService: ChatbotService
) {

    @Operation(
        summary = "Enviar mensajes al chatbot de Agendify",
        description = "Recibe el historial de conversacion y devuelve una respuesta del asistente de Agendify.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = [
                Content(
                    mediaType = "application/json",
                    schema = Schema(implementation = ChatbotRequest::class),
                    examples = [
                        ExampleObject(
                            name = "Consulta de reserva",
                            value = """{
  "authenticated": false,
  "userRole": "CLIENTE",
  "messages": [
    { "role": "user", "content": "Como reservo un turno?" }
  ]
}"""
                        ),
                        ExampleObject(
                            name = "Abrir menu principal",
                            value = """{
  "messages": [],
  "action": {
    "type": "MAIN_MENU",
    "targetNodeId": "main_menu"
  }
}"""
                        )
                    ]
                )
            ]
        )
    )
    @ApiResponse(
        responseCode = "200",
        description = "Respuesta generada correctamente",
        content = [
            Content(
                mediaType = "application/json",
                schema = Schema(implementation = ChatbotResponse::class),
                examples = [
                    ExampleObject(
                        name = "Respuesta exitosa",
                        value = """{
  "message": "Hola, soy el asistente de Agendify. Puedo ayudarte a reservar turnos, entender pagos y senas, configurar agendas o resolver dudas sobre roles.",
  "source": "menu",
  "options": [
    { "id": "reserve_menu", "label": "Reservar turnos", "type": "OPEN_NODE", "targetNodeId": "reserve_menu" },
    { "id": "specialist_menu", "label": "Para especialistas", "type": "OPEN_NODE", "targetNodeId": "specialist_menu" }
  ],
  "currentNodeId": "main_menu",
  "previousNodeId": null,
  "suggestedActions": []
}"""
                    )
                ]
            )
        ]
    )
    @ApiResponse(responseCode = "400", description = "Request invalido o sin mensajes")
    @PostMapping("/messages")
    fun chat(@RequestBody request: ChatbotRequest): ChatbotResponse =
        chatbotService.reply(request)
}
