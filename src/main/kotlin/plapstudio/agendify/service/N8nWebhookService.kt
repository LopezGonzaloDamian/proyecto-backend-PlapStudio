package plapstudio.agendify.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class N8nWebhookService(
    builder: RestClient.Builder,
    @Value("\${agendify.integrations.n8n.turno-confirmado-url:}") private val turnoConfirmadoUrl: String
) {
    private val logger = LoggerFactory.getLogger(N8nWebhookService::class.java)
    private val client = builder.build()

    fun notificarTurnoConfirmado(payload: TurnoConfirmadoWebhookPayload) {
        if (turnoConfirmadoUrl.isBlank()) return

        runCatching {
            client.post()
                .uri(turnoConfirmadoUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .toBodilessEntity()
        }.onFailure { error ->
            logger.warn("No se pudo notificar el turno confirmado a n8n: {}", error.message)
        }
    }
}

data class TurnoConfirmadoWebhookPayload(
    val nombre: String,
    val mail: String?,
    val profesional: String,
    val especialidad: String,
    @JsonProperty("fecha_turno")
    val fechaTurno: String,
    @JsonProperty("hora_turno")
    val horaTurno: String
)
