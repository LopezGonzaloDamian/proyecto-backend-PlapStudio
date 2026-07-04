package plapstudio.agendify

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AuthIntegrationTest : IntegrationTestSupport() {

    @Test
    fun `registro guarda contrasena hasheada y no la expone`() {
        role("CLIENTE")

        mockMvc.perform(
            post("/auth/registro")
                .contentType(jsonUtf8)
                .content(
                    json(
                        mapOf(
                            "email" to "nuevo@agendify.com",
                            "password" to "1234",
                            "nombreCompleto" to "Nuevo Usuario",
                            "telefono" to "12345678",
                            "rol" to "CLIENTE"
                        )
                    )
                )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").isNotEmpty)
            .andExpect(jsonPath("$.usuario.email").value("nuevo@agendify.com"))
            .andExpect(jsonPath("$.usuario.contrasenaHash").doesNotExist())

        val usuario = usuarioRepository.findByEmail("nuevo@agendify.com")!!
        assertFalse(usuario.contrasenaHash == "1234")
        assertTrue(passwordService.matches("1234", usuario.contrasenaHash))
    }

    @Test
    fun `login rechaza password invalida`() {
        cliente(email = "cliente@login.com", password = "1234")

        mockMvc.perform(
            post("/auth/login")
                .contentType(jsonUtf8)
                .content(json(mapOf("email" to "cliente@login.com", "password" to "0000")))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `login migra password legacy en texto plano`() {
        val usuario = usuarioRepository.save(
            plapstudio.agendify.domain.Usuario(
                email = "legacy@agendify.com",
                contrasenaHash = "1234",
                nombreCompleto = "Legacy User",
                telefono = "123123123",
                roles = mutableSetOf(role("CLIENTE"))
            )
        )
        val perfil = perfilClienteRepository.save(plapstudio.agendify.domain.PerfilCliente(usuario = usuario))
        usuario.perfilCliente = perfil
        usuarioRepository.save(usuario)

        mockMvc.perform(
            post("/auth/login")
                .contentType(jsonUtf8)
                .content(json(mapOf("email" to "legacy@agendify.com", "password" to "1234")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").isNotEmpty)

        val actualizado = usuarioRepository.findByEmail("legacy@agendify.com")!!
        assertFalse(actualizado.contrasenaHash == "1234")
        assertTrue(passwordService.matches("1234", actualizado.contrasenaHash))
    }
}
