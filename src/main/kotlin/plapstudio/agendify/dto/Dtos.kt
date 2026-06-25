package plapstudio.agendify.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class LoginRequest(
    @field:Email(message = "El email no es valido")
    @field:NotBlank(message = "El email es obligatorio")
    val email: String,
    @field:NotBlank(message = "La contrasena es obligatoria")
    @field:Size(min = 4, max = 128, message = "La contrasena debe tener entre 4 y 128 caracteres")
    val password: String
)

data class GoogleLoginRequest(
    @field:NotBlank(message = "La credencial de Google es obligatoria")
    val credential: String = ""
)

data class RegistroRequest(
    @field:Email(message = "El email no es valido")
    @field:NotBlank(message = "El email es obligatorio")
    val email: String,
    @field:NotBlank(message = "La contrasena es obligatoria")
    @field:Size(min = 4, max = 128, message = "La contrasena debe tener entre 4 y 128 caracteres")
    val password: String,
    @field:NotBlank(message = "El nombre completo es obligatorio")
    @field:Size(max = 120, message = "El nombre completo no puede superar los 120 caracteres")
    val nombreCompleto: String,
    @field:NotBlank(message = "El telefono es obligatorio")
    @field:Size(max = 40, message = "El telefono no puede superar los 40 caracteres")
    val telefono: String,
    @field:NotBlank(message = "El rol es obligatorio")
    val rol: String,
    val especialidad: String? = null,
    val biografia: String? = null,
    val localidad: String? = null,
    val direccion: String? = null,
    @field:Positive(message = "El precio debe ser mayor a cero")
    val precio: BigDecimal? = null,
    @field:Size(max = 20, message = "No se pueden enviar mas de 20 servicios")
    val servicios: List<String>? = null,
    @field:Valid
    val serviciosConPrecio: List<ServicioProfesionalDto>? = null
)

data class SeleccionRolRequest(
    @field:NotBlank(message = "El rol es obligatorio")
    val rol: String = "",
    val especialidad: String? = null,
    val biografia: String? = null,
    val localidad: String? = null,
    val direccion: String? = null,
    @field:Positive(message = "El precio debe ser mayor a cero")
    val precio: BigDecimal? = null,
    @field:Size(max = 20, message = "No se pueden enviar mas de 20 servicios")
    val servicios: List<String>? = null,
    @field:Valid
    val serviciosConPrecio: List<ServicioProfesionalDto>? = null
)

data class ActivarRolRequest(
    @field:NotBlank(message = "El rol es obligatorio")
    val rol: String = ""
)

data class AuthResponse(
    val token: String,
    val usuario: UsuarioDto
)

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

data class UsuarioUpdateRequest(
    @field:NotBlank(message = "El nombre completo es obligatorio")
    @field:Size(max = 120, message = "El nombre completo no puede superar los 120 caracteres")
    val nombreCompleto: String,
    @field:NotBlank(message = "El telefono es obligatorio")
    @field:Size(max = 40, message = "El telefono no puede superar los 40 caracteres")
    val telefono: String,
    val urlAvatar: String = ""
)

data class ServicioProfesionalDto(
    @field:NotBlank(message = "El nombre del servicio es obligatorio")
    @field:Size(max = 100, message = "El nombre del servicio no puede superar los 100 caracteres")
    val nombre: String,
    @field:Positive(message = "El precio del servicio debe ser mayor a cero")
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
    @field:NotBlank(message = "La especialidad es obligatoria")
    @field:Size(max = 120, message = "La especialidad no puede superar los 120 caracteres")
    val especialidad: String,
    @field:NotBlank(message = "La biografia es obligatoria")
    @field:Size(max = 1000, message = "La biografia no puede superar los 1000 caracteres")
    val biografia: String,
    val urlAvatar: String,
    @field:NotBlank(message = "La localidad es obligatoria")
    @field:Size(max = 120, message = "La localidad no puede superar los 120 caracteres")
    val localidad: String,
    @field:NotBlank(message = "La direccion es obligatoria")
    @field:Size(max = 180, message = "La direccion no puede superar los 180 caracteres")
    val direccion: String,
    @field:PositiveOrZero(message = "El precio no puede ser negativo")
    val precio: BigDecimal,
    val cobertura: String,
    val matriculaNacional: String,
    val matriculaProvincial: String,
    @field:NotEmpty(message = "Debe haber al menos un servicio")
    @field:Size(max = 20, message = "No se pueden enviar mas de 20 servicios")
    val servicios: List<String>,
    @field:Valid
    val serviciosConPrecio: List<ServicioProfesionalDto> = emptyList()
)

data class ClienteDto(
    val id: Long,
    val nombreCompleto: String,
    val email: String,
    val telefono: String,
    val notas: String
)

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
    @field:Positive(message = "El profesional debe ser valido")
    val profesionalId: Long,
    @field:NotBlank(message = "El nombre de la agenda es obligatorio")
    @field:Size(max = 120, message = "El nombre de la agenda no puede superar los 120 caracteres")
    val nombre: String,
    @field:NotBlank(message = "La descripcion de la agenda es obligatoria")
    @field:Size(max = 300, message = "La descripcion de la agenda no puede superar los 300 caracteres")
    val descripcion: String
)

data class AgendaUpdateRequest(
    @field:NotBlank(message = "El nombre de la agenda es obligatorio")
    @field:Size(max = 120, message = "El nombre de la agenda no puede superar los 120 caracteres")
    val nombre: String,
    @field:NotBlank(message = "La descripcion de la agenda es obligatoria")
    @field:Size(max = 300, message = "La descripcion de la agenda no puede superar los 300 caracteres")
    val descripcion: String,
    val activa: Boolean
)

data class ConfiguracionHorariaDto(
    val id: UUID?,
    @field:NotNull(message = "El dia de la semana es obligatorio")
    val diaSemana: DayOfWeek,
    @field:NotNull(message = "La hora de inicio es obligatoria")
    val inicioSlot: LocalTime,
    @field:NotNull(message = "La hora de fin es obligatoria")
    val finSlot: LocalTime,
    @field:Positive(message = "La duracion del slot debe ser mayor a cero")
    val duracionSlotMinutos: Int
)

data class ExcepcionAgendaDto(
    val id: UUID?,
    @field:NotNull(message = "La fecha de inicio es obligatoria")
    val fechaInicio: LocalDate,
    @field:NotNull(message = "La fecha de fin es obligatoria")
    val fechaFin: LocalDate,
    @field:NotBlank(message = "El motivo es obligatorio")
    @field:Size(max = 200, message = "El motivo no puede superar los 200 caracteres")
    val motivo: String
)

data class SlotDto(
    val iniciaEn: LocalDateTime,
    val duracionMinutos: Int,
    val disponible: Boolean
)

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
    @field:NotNull(message = "La agenda es obligatoria")
    val agendaId: UUID,
    @field:Positive(message = "El cliente debe ser valido")
    val clienteId: Long? = null,
    val clienteExternoNombre: String? = null,
    val clienteExternoTelefono: String? = null,
    val clienteExternoDni: String? = null,
    @field:Email(message = "El email del cliente externo no es valido")
    val clienteExternoEmail: String? = null,
    @field:NotNull(message = "La fecha y hora del turno es obligatoria")
    val iniciaEn: LocalDateTime,
    @field:Positive(message = "La duracion debe ser mayor a cero")
    val duracionMinutos: Int,
    @field:Size(max = 600, message = "Las notas no pueden superar los 600 caracteres")
    val notas: String = "",
    @field:Positive(message = "El precio del servicio debe ser mayor a cero")
    val precioServicio: BigDecimal? = null,
    val pagarAlReservar: Boolean = false,
    val medioPago: String? = null
)

data class TurnoUpdateRequest(
    @field:NotNull(message = "La fecha y hora del turno es obligatoria")
    val iniciaEn: LocalDateTime,
    @field:Positive(message = "La duracion debe ser mayor a cero")
    val duracionMinutos: Int,
    @field:Size(max = 600, message = "Las notas no pueden superar los 600 caracteres")
    val notas: String,
    @field:NotBlank(message = "El estado es obligatorio")
    val estado: String
)

data class TurnoNotasRequest(
    @field:Size(max = 600, message = "Las notas no pueden superar los 600 caracteres")
    val notas: String
)

data class TurnoCancelRequest(
    @field:Size(max = 200, message = "El motivo no puede superar los 200 caracteres")
    val motivo: String? = null
)

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

data class FavoritoDto(
    val id: UUID,
    val clienteId: Long,
    val profesional: ProfesionalSummaryDto,
    val agregadoEn: LocalDateTime
)

data class FavoritoToggleRequest(
    @field:Positive(message = "El cliente debe ser valido")
    val clienteId: Long,
    @field:Positive(message = "El profesional debe ser valido")
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
    @field:Positive(message = "El profesional debe ser valido")
    val profesionalId: Long,
    @field:Email(message = "El email del asistente no es valido")
    @field:NotBlank(message = "El email del asistente es obligatorio")
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
    @field:NotNull(message = "El turno es obligatorio")
    val turnoId: UUID,
    @field:Min(value = 1, message = "La calificacion debe estar entre 1 y 5")
    @field:Max(value = 5, message = "La calificacion debe estar entre 1 y 5")
    val calificacion: Int,
    @field:Size(max = 600, message = "El comentario no puede superar los 600 caracteres")
    val comentario: String = ""
)
