package plapstudio.agendify

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

class TurnoAuthorizationIntegrationTest : IntegrationTestSupport() {

    @Test
    fun `cliente no puede editar notas internas ni estado`() {
        val (_, perfilProfesional) = profesional()
        val (_, perfilCliente) = cliente()
        val agenda = agenda(perfilProfesional)
        configurarAgenda(agenda, diaSemana = LocalDateTime.now().plusDays(2).dayOfWeek)

        val reservaResponse = mockMvc.perform(
            post("/turnos")
                .header("Authorization", authHeader(perfilCliente.usuario))
                .contentType(jsonUtf8)
                .content(
                    json(
                        mapOf(
                            "agendaId" to agenda.id,
                            "clienteId" to perfilCliente.id,
                            "iniciaEn" to LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0),
                            "duracionMinutos" to 30,
                            "notas" to "Consulta inicial"
                        )
                    )
                )
        )
            .andExpect(status().isOk)
            .andReturn()

        val turnoId = objectMapper.readTree(reservaResponse.response.contentAsString).get("id").asText()

        mockMvc.perform(
            patch("/turnos/$turnoId/notas")
                .header("Authorization", authHeader(perfilCliente.usuario))
                .contentType(jsonUtf8)
                .content(json(mapOf("notas" to "nota interna")))
        )
            .andExpect(status().isForbidden)

        mockMvc.perform(
            put("/turnos/$turnoId")
                .header("Authorization", authHeader(perfilCliente.usuario))
                .contentType(jsonUtf8)
                .content(
                    json(
                        mapOf(
                            "iniciaEn" to LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0),
                            "duracionMinutos" to 30,
                            "notas" to "cambio indebido",
                            "estado" to "CANCELADO"
                        )
                    )
                )
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `cliente puede cancelar su propio turno`() {
        val (_, perfilProfesional) = profesional(email = "prof2@agendify.com")
        val (usuarioCliente, perfilCliente) = cliente(email = "cliente2@agendify.com")
        val agenda = agenda(perfilProfesional, nombre = "Agenda 2")
        val fecha = LocalDateTime.now().plusDays(3).withHour(11).withMinute(0).withSecond(0).withNano(0)
        configurarAgenda(agenda, diaSemana = fecha.dayOfWeek)

        val reservaResponse = mockMvc.perform(
            post("/turnos")
                .header("Authorization", authHeader(usuarioCliente))
                .contentType(jsonUtf8)
                .content(
                    json(
                        mapOf(
                            "agendaId" to agenda.id,
                            "clienteId" to perfilCliente.id,
                            "iniciaEn" to fecha,
                            "duracionMinutos" to 30
                        )
                    )
                )
        )
            .andExpect(status().isOk)
            .andReturn()

        val turnoId = objectMapper.readTree(reservaResponse.response.contentAsString).get("id").asText()

        mockMvc.perform(
            patch("/turnos/$turnoId/cancelar")
                .header("Authorization", authHeader(usuarioCliente))
                .contentType(jsonUtf8)
                .content(json(mapOf("motivo" to "No llego")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.estado").value("CANCELADO"))
    }

    @Test
    fun `asistente asignado puede gestionar turno`() {
        val (_, perfilProfesional) = profesional(email = "prof3@agendify.com")
        val asistente = asistente()
        val (_, perfilCliente) = cliente(email = "cliente3@agendify.com")
        val agenda = agenda(perfilProfesional, nombre = "Agenda 3")
        val fecha = LocalDateTime.now().plusDays(4).withHour(9).withMinute(0).withSecond(0).withNano(0)
        configurarAgenda(agenda, diaSemana = fecha.dayOfWeek)
        vincularAsistente(perfilProfesional, asistente)

        val turno = turnoRepository.save(
            plapstudio.agendify.domain.Turno(
                agenda = agenda,
                cliente = perfilCliente,
                iniciaEn = fecha,
                duracionMinutos = 30,
                notas = "Original"
            )
        )

        mockMvc.perform(
            patch("/turnos/${turno.id}/notas")
                .header("Authorization", authHeader(asistente))
                .contentType(jsonUtf8)
                .content(json(mapOf("notas" to "Actualizada por asistente")))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.notas").value("Actualizada por asistente"))
    }
}
