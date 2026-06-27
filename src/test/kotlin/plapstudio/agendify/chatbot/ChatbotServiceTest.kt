package plapstudio.agendify.chatbot

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import plapstudio.agendify.auth.AuthContext
import plapstudio.agendify.auth.AuthPrincipal
import plapstudio.agendify.domain.Rol
import plapstudio.agendify.domain.Usuario
import plapstudio.agendify.repository.UsuarioRepository
import java.util.Optional

class ChatbotServiceTest {

    private val authContext = mockk<AuthContext>()
    private val usuarioRepository = mockk<UsuarioRepository>()
    private val knowledgeBase = ChatbotKnowledgeBase()
    private val promptFactory = mockk<ChatbotPromptFactory>()
    private val geminiChatClient = mockk<GeminiChatClient>()

    @Test
    fun `ignora rol inventado si no hay sesion real`() {
        val service = ChatbotService(
            authContext = authContext,
            usuarioRepository = usuarioRepository,
            chatbotKnowledgeBase = knowledgeBase,
            chatbotPromptFactory = promptFactory,
            geminiChatClient = geminiChatClient,
            geminiEnabled = false,
            geminiApiKey = "",
            geminiModel = "gemini-2.0-flash"
        )

        every { authContext.get() } returns null

        val response = service.reply(
            ChatbotRequest(
                authenticated = true,
                userRole = "ADMIN",
                messages = listOf(ChatbotMessage(role = "user", content = "Como reservar un turno?"))
            )
        )

        assertEquals("intent", response.source)
        assertEquals("reserve_how_to", response.currentNodeId)
        assertTrue(response.message.contains("Para reservar un turno"))
    }

    @Test
    fun `resuelve rol real desde usuario autenticado y conserva rol activo valido`() {
        val service = ChatbotService(
            authContext = authContext,
            usuarioRepository = usuarioRepository,
            chatbotKnowledgeBase = knowledgeBase,
            chatbotPromptFactory = promptFactory,
            geminiChatClient = geminiChatClient,
            geminiEnabled = false,
            geminiApiKey = "",
            geminiModel = "gemini-2.0-flash"
        )

        val usuario = Usuario(
            id = 8L,
            email = "multi@agendify.com",
            contrasenaHash = "hash",
            nombreCompleto = "Multi Role",
            telefono = "123",
            roles = mutableSetOf(Rol(nombre = "PROFESIONAL"), Rol(nombre = "ASISTENTE"))
        )

        every { authContext.get() } returns AuthPrincipal(8L)
        every { usuarioRepository.findById(8L) } returns Optional.of(usuario)

        val response = service.reply(
            ChatbotRequest(
                authenticated = true,
                userRole = "ASISTENTE",
                messages = listOf(ChatbotMessage(role = "user", content = "Que puede hacer un asistente?"))
            )
        )

        assertEquals("intent", response.source)
        assertEquals("roles_assistant", response.currentNodeId)
        assertTrue(response.message.contains("Un asistente ayuda al profesional"))
    }

    @Test
    fun `si gemini falla devuelve fallback guiado sin romper opciones`() {
        val service = ChatbotService(
            authContext = authContext,
            usuarioRepository = usuarioRepository,
            chatbotKnowledgeBase = knowledgeBase,
            chatbotPromptFactory = promptFactory,
            geminiChatClient = geminiChatClient,
            geminiEnabled = true,
            geminiApiKey = "api-key",
            geminiModel = "gemini-2.0-flash"
        )

        every { authContext.get() } returns null
        every { promptFactory.buildPrompt(any(), any()) } returns "prompt"
        every { geminiChatClient.generate(any(), any()) } throws RuntimeException("quota exceeded")

        val response = service.reply(
            ChatbotRequest(
                messages = listOf(ChatbotMessage(role = "user", content = "Hola"))
            )
        )

        assertEquals("menu", response.source)
        assertEquals(ChatbotKnowledgeBase.MAIN_MENU_ID, response.currentNodeId)
        assertEquals(7, response.options.size)
        verify(exactly = 0) { geminiChatClient.generate(any(), any()) }
    }

    @Test
    fun `main menu puede abrirse sin historial previo`() {
        val service = ChatbotService(
            authContext = authContext,
            usuarioRepository = usuarioRepository,
            chatbotKnowledgeBase = knowledgeBase,
            chatbotPromptFactory = promptFactory,
            geminiChatClient = geminiChatClient,
            geminiEnabled = false,
            geminiApiKey = "",
            geminiModel = "gemini-2.0-flash"
        )

        every { authContext.get() } returns null

        val response = service.reply(
            ChatbotRequest(
                action = ChatbotActionRequest(
                    type = ChatbotOptionType.MAIN_MENU,
                    targetNodeId = ChatbotKnowledgeBase.MAIN_MENU_ID
                )
            )
        )

        assertEquals("menu", response.source)
        assertEquals(ChatbotKnowledgeBase.MAIN_MENU_ID, response.currentNodeId)
        assertNull(response.previousNodeId)
    }
}
