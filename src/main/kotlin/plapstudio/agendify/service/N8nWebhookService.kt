package plapstudio.agendify.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import plapstudio.agendify.config.N8nProperties
import plapstudio.agendify.domain.Turno
import plapstudio.agendify.service.dto.N8nWebhookResponse
import plapstudio.agendify.service.dto.TurnoConfirmadoWebhookRequest

@Service
class N8nWebhookService(
    restClientBuilder: RestClient.Builder,
    private val n8nProperties: N8nProperties
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val restClient = restClientBuilder.build()

    fun notificarTurnoConfirmado(turno: Turno) {
        val payload = TurnoConfirmadoWebhookRequest(
            mail = turno.cliente.usuario.email,
            telefono = turno.cliente.usuario.telefono,
            nombre = turno.cliente.usuario.nombreCompleto,
            profesional = turno.agenda.profesional.usuario.nombreCompleto,
            especialidad = turno.agenda.profesional.especialidad,
            fecha_turno = turno.iniciaEn.toLocalDate().toString(),
            hora_turno = turno.iniciaEn.toLocalTime().toString()
        )

        runCatching {
            restClient.post()
                .uri(n8nProperties.turnoConfirmadoUrl)
                .body(payload)
                .retrieve()
                .body(N8nWebhookResponse::class.java)
        }.onSuccess { response ->
            when {
                response == null -> logger.warn("n8n respondio sin body al confirmar turno {}", turno.id)
                response.success -> logger.info("Webhook n8n enviado para turno {}: {}", turno.id, response.message)
                else -> logger.warn("n8n respondio success=false para turno {}: {}", turno.id, response.message)
            }
        }.onFailure { error ->
            logger.error("Fallo el envio del webhook n8n para el turno {}", turno.id, error)
        }
    }
}
