package plapstudio.agendify

import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.DayOfWeek
import java.time.LocalTime

class AgendaSecurityValidationIntegrationTest : IntegrationTestSupport() {

    @Test
    fun `endpoint privado de agenda requiere autenticacion`() {
        val (_, perfilProfesional) = profesional(email = "privado@agendify.com")
        val agenda = agenda(perfilProfesional)
        configurarAgenda(agenda)

        mockMvc.perform(get("/agendas/${agenda.id}"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `agendas activas publicas no exponen configuraciones internas`() {
        val (_, perfilProfesional) = profesional(email = "publica@agendify.com")
        val agenda = agenda(perfilProfesional, nombre = "Agenda publica")
        configurarAgenda(agenda)

        mockMvc.perform(get("/agendas/activas"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].configuraciones").doesNotExist())
            .andExpect(jsonPath("$[0].nombre").value("Agenda publica"))
    }

    @Test
    fun `configuracion horaria invalida se rechaza`() {
        val (usuarioProfesional, perfilProfesional) = profesional(email = "config@agendify.com")
        val agenda = agenda(perfilProfesional, nombre = "Agenda config")

        mockMvc.perform(
            put("/agendas/${agenda.id}/configuraciones")
                .header("Authorization", authHeader(usuarioProfesional))
                .contentType(jsonUtf8)
                .content(
                    json(
                        listOf(
                            mapOf(
                                "diaSemana" to DayOfWeek.MONDAY,
                                "inicioSlot" to LocalTime.of(18, 0),
                                "finSlot" to LocalTime.of(9, 0),
                                "duracionSlotMinutos" to 30
                            )
                        )
                    )
                )
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("La hora de inicio debe ser anterior a la hora de fin"))
    }

    @Test
    fun `crear agenda con nombre vacio devuelve error claro`() {
        val (usuarioProfesional, perfilProfesional) = profesional(email = "agenda@agendify.com")

        mockMvc.perform(
            post("/agendas")
                .header("Authorization", authHeader(usuarioProfesional))
                .contentType(jsonUtf8)
                .content(
                    json(
                        mapOf(
                            "profesionalId" to perfilProfesional.id,
                            "nombre" to "",
                            "descripcion" to "Agenda"
                        )
                    )
                )
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.details[0]").exists())
    }
}
