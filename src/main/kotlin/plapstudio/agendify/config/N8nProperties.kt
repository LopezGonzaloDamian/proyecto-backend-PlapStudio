package plapstudio.agendify.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "integrations.n8n")
data class N8nProperties(
    val turnoConfirmadoUrl: String
)
