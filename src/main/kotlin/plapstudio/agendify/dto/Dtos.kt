package plapstudio.agendify.dto

import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

// ──────────────────────────────────────────────────────────────────────────────
// Auth
// ──────────────────────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class GoogleLoginRequest(
    val credential: String = ""
)

data class RegistroRequest(
    val email: String,
    val password: String,
    val nombreCompleto: String,
    val telefono: String,
    val rol: String, // "CLIENTE" | "PROFESIONAL" | "ASISTENTE"
    val especialidad: String? = null,
    val biografia: String? = null,
    val localidad: String? = null,
    val direccion: String? = null,
    val precio: BigDecimal? = null,
    val servicios: List<String>? = null,
    val serviciosConPrecio: List<ServicioProfesionalDto>? = null
)

data class SeleccionRolRequest(
    val rol: String = "",
    val especialidad: String? = null,
    val biografia: String? = null,
    val localidad: String? = null,
    val direccion: String? = null,
    val precio: BigDecimal? = null,
    val servicios: List<String>? = null,
    val serviciosConPrecio: List<ServicioProfesionalDto>? = null
)

data class ActivarRolRequest(
    val rol: String = ""
)

data class AuthResponse(
    val token: String,
    val usuario: UsuarioDto
)

// ──────────────────────────────────────────────────────────────────────────────
// Usuario
// ──────────────────────────────────────────────────────────────────────────────

data class UsuarioDto(
    val id: Long,
    val email: String,
    val nombreCompleto: String,
    val telefono: String,
    val urlAvatar: String,
    val activo: Boolean,
    val roles: List<String>,
    val perfilProfesionalId: Long?,
    val perfilClienteId: Long?,
    val requiereSeleccionRol: Boolean
)

// ──────────────────────────────────────────────────────────────────────────────
// Profesional
// ──────────────────────────────────────────────────────────────────────────────

data class UsuarioUpdateRequest(
    val nombreCompleto: String,
    val telefono: String,
    val urlAvatar: String = ""
)

data class ServicioProfesionalDto(
    val nombre: String,
    val precio: BigDecimal
)

data class ProfesionalDto(
    val id: Long,
    val nombreCompleto: String,
    val email: String,
    val telefono: String,
    val especialidad: String,
    val biografia: String,
    val urlAvatar: String,
    val destacado: Boolean,
    val localidad: String,
    val direccion: String,
    val precio: BigDecimal,
    val comisionPendientePorcentaje: BigDecimal,
    val cobertura: String,
    val matriculaNacional: String,
    val matriculaProvincial: String,
    val servicios: List<String>,
    val serviciosConPrecio: List<ServicioProfesionalDto>,
    val agendas: List<AgendaResumenDto>
)

data class ProfesionalSummaryDto(
    val id: Long,
    val nombreCompleto: String,
    val especialidad: String,
    val urlAvatar: String,
    val localidad: String,
    val precio: BigDecimal,
    val destacado: Boolean,
    val servicios: List<String>,
    val serviciosConPrecio: List<ServicioProfesionalDto>
)

data class ProfesionalUpdateRequest(
    val especialidad: String,
    val biografia: String,
    val urlAvatar: String,
    val localidad: String,
    val direccion: String,
    val precio: BigDecimal,
    val cobertura: String,
    val matriculaNacional: String,
    val matriculaProvincial: String,
    val servicios: List<String>,
    val serviciosConPrecio: List<ServicioProfesionalDto> = emptyList()
)

// ──────────────────────────────────────────────────────────────────────────────
// Cliente
// ──────────────────────────────────────────────────────────────────────────────

data class ClienteDto(
    val id: Long,
    val nombreCompleto: String,
    val email: String,
    val telefono: String,
    val notas: String
)

// ──────────────────────────────────────────────────────────────────────────────
// Agenda + configuración horaria
// ──────────────────────────────────────────────────────────────────────────────

data class AgendaResumenDto(
    val id: UUID,
    val nombre: String,
    val descripcion: String,
    val activa: Boolean
)

data class AgendaDto(
    val id: UUID,
    val nombre: String,
    val descripcion: String,
    val activa: Boolean,
    val profesionalId: Long,
    val profesionalNombre: String,
    val configuraciones: List<ConfiguracionHorariaDto>,
    val excepciones: List<ExcepcionAgendaDto>
)

data class AgendaCreateRequest(
    val profesionalId: Long,
    val nombre: String,
    val descripcion: String
)

data class AgendaUpdateRequest(
    val nombre: String,
    val descripcion: String,
    val activa: Boolean
)

data class ConfiguracionHorariaDto(
    val id: UUID?,
    val diaSemana: DayOfWeek,
    val inicioSlot: LocalTime,
    val finSlot: LocalTime,
    val duracionSlotMinutos: Int
)

data class ExcepcionAgendaDto(
    val id: UUID?,
    val fechaInicio: LocalDate,
    val fechaFin: LocalDate,
    val motivo: String
)

data class SlotDto(
    val iniciaEn: LocalDateTime,
    val duracionMinutos: Int,
    val disponible: Boolean
)

// ──────────────────────────────────────────────────────────────────────────────
// Turno
// ──────────────────────────────────────────────────────────────────────────────

data class TurnoDto(
    val id: UUID,
    val agendaId: UUID,
    val agendaNombre: String,
    val profesionalId: Long,
    val profesionalNombre: String,
    val clienteId: Long?,
    val clienteNombre: String,
    val clienteTelefono: String?,
    val clienteDni: String?,
    val clienteEmail: String?,
    val iniciaEn: LocalDateTime,
    val duracionMinutos: Int,
    val estado: String,
    val precio: BigDecimal,
    val notas: String,
    val pago: PagoDto?
)

data class TurnoCreateRequest(
    val agendaId: UUID,
    val clienteId: Long? = null,
    val clienteExternoNombre: String? = null,
    val clienteExternoTelefono: String? = null,
    val clienteExternoDni: String? = null,
    val clienteExternoEmail: String? = null,
    val iniciaEn: LocalDateTime,
    val duracionMinutos: Int,
    val notas: String = "",
    val precioServicio: BigDecimal? = null,
    val pagarAlReservar: Boolean = false,
    val medioPago: String? = null
)

data class TurnoUpdateRequest(
    val iniciaEn: LocalDateTime,
    val duracionMinutos: Int,
    val notas: String,
    val estado: String
)

data class TurnoNotasRequest(
    val notas: String
)

data class TurnoCancelRequest(
    val motivo: String? = null
)

// ──────────────────────────────────────────────────────────────────────────────
// Pago / factura
// ──────────────────────────────────────────────────────────────────────────────

data class PagoDto(
    val id: UUID,
    val turnoId: UUID,
    val monto: BigDecimal,
    val moneda: String,
    val porcentajeComision: BigDecimal,
    val montoComision: BigDecimal,
    val estado: String,
    val origen: String,
    val referenciaProveedorMock: String?,
    val pagadoEn: LocalDateTime?
)

data class FacturaDto(
    val id: UUID,
    val pagoId: UUID,
    val clienteId: Long,
    val clienteNombre: String,
    val profesionalId: Long,
    val profesionalNombre: String,
    val numeroFactura: String,
    val montoTotal: BigDecimal,
    val moneda: String,
    val estado: String,
    val emitidaEn: LocalDateTime
)

// ──────────────────────────────────────────────────────────────────────────────
// Favorito / notificación / asistente
// ──────────────────────────────────────────────────────────────────────────────

data class FavoritoDto(
    val id: UUID,
    val clienteId: Long,
    val profesional: ProfesionalSummaryDto,
    val agregadoEn: LocalDateTime
)

data class FavoritoToggleRequest(
    val clienteId: Long,
    val profesionalId: Long
)

data class NotificacionDto(
    val id: UUID,
    val usuarioId: Long,
    val canal: String,
    val titulo: String,
    val cuerpo: String,
    val leida: Boolean,
    val enviadaEn: LocalDateTime,
    val recursoTipo: String?,
    val recursoId: UUID?
)

data class AsistenteAsignacionDto(
    val id: UUID,
    val profesionalId: Long,
    val profesionalNombre: String,
    val profesionalEspecialidad: String,
    val profesionalAvatarUrl: String?,
    val asistenteId: Long,
    val asistenteNombre: String,
    val asistenteEmail: String,
    val estado: String,
    val asignadoEn: LocalDateTime
)

data class AsistenteAsignarRequest(
    val profesionalId: Long,
    val asistenteEmail: String
)

data class ResenaDto(
    val id: UUID,
    val profesionalId: Long,
    val clienteId: Long,
    val clienteNombre: String,
    val clienteIniciales: String,
    val turnoId: UUID,
    val calificacion: Int,
    val comentario: String,
    val creadaEn: LocalDateTime
)

data class ResenaCreateRequest(
    val turnoId: UUID,
    val calificacion: Int,
    val comentario: String = ""
)
