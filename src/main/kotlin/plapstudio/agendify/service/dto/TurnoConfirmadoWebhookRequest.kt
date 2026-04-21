package plapstudio.agendify.service.dto

data class TurnoConfirmadoWebhookRequest(
    val mail: String,
    val telefono: String,
    val nombre: String,
    val profesional: String,
    val especialidad: String,
    val fecha_turno: String,
    val hora_turno: String
)
