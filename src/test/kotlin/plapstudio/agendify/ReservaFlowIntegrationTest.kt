package plapstudio.agendify

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class ReservaFlowIntegrationTest : IntegrationTestSupport() {

    @Test
    fun `no permite doble reserva en el mismo slot`() {
        val (_, perfilProfesional) = profesional(email = "turnos@agendify.com")
        val (usuarioCliente1, perfilCliente1) = cliente(email = "clientea@agendify.com")
        val (usuarioCliente2, perfilCliente2) = cliente(email = "clienteb@agendify.com")
        val agenda = agenda(perfilProfesional, nombre = "Agenda reservas")
        val fecha = LocalDateTime.now().plusDays(5).withHour(15).withMinute(0).withSecond(0).withNano(0)
        configurarAgenda(agenda, diaSemana = fecha.dayOfWeek)

        val payload = mapOf(
            "agendaId" to agenda.id,
            "iniciaEn" to fecha,
            "duracionMinutos" to 30
        )

        mockMvc.perform(
            post("/turnos")
                .header("Authorization", authHeader(usuarioCliente1))
                .contentType(jsonUtf8)
                .content(json(payload + ("clienteId" to perfilCliente1.id)))
        )
            .andExpect(status().isOk)

        mockMvc.perform(
            post("/turnos")
                .header("Authorization", authHeader(usuarioCliente2))
                .contentType(jsonUtf8)
                .content(json(payload + ("clienteId" to perfilCliente2.id)))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `cliente solo puede consultar sus propios turnos`() {
        val (_, perfilProfesional) = profesional(email = "privacidad@agendify.com")
        val (usuarioCliente1, perfilCliente1) = cliente(email = "owner@agendify.com")
        val (usuarioCliente2, perfilCliente2) = cliente(email = "otro@agendify.com")
        val agenda = agenda(perfilProfesional, nombre = "Agenda privacidad")
        val fecha = LocalDateTime.now().plusDays(6).withHour(16).withMinute(0).withSecond(0).withNano(0)
        configurarAgenda(agenda, diaSemana = fecha.dayOfWeek)

        mockMvc.perform(
            post("/turnos")
                .header("Authorization", authHeader(usuarioCliente1))
                .contentType(jsonUtf8)
                .content(
                    json(
                        mapOf(
                            "agendaId" to agenda.id,
                            "clienteId" to perfilCliente1.id,
                            "iniciaEn" to fecha,
                            "duracionMinutos" to 30
                        )
                    )
                )
        )
            .andExpect(status().isOk)

        mockMvc.perform(
            get("/turnos/cliente/${perfilCliente1.id}")
                .header("Authorization", authHeader(usuarioCliente2))
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            get("/turnos/cliente/${perfilCliente2.id}")
                .header("Authorization", authHeader(usuarioCliente2))
        )
            .andExpect(status().isOk)
    }
}
