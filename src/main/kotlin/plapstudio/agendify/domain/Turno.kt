package plapstudio.agendify.domain

import plapstudio.agendify.errors.BusinessException
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "turnos")
class Turno(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agenda_id", nullable = false)
    val agenda: Agenda,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "email_cliente", nullable = false)
    val cliente: PerfilCliente,

    var iniciaEn: LocalDateTime,
    var duracionMinutos: Int,

    @Enumerated(EnumType.STRING)
    var estado: EstadoTurno = EstadoTurno.PENDIENTE,

    var notas: String = "",
    val creadoEn: LocalDateTime = LocalDateTime.now(),
    var actualizadoEn: LocalDateTime = LocalDateTime.now()
) {
    fun confirmar() {
        if (estado == EstadoTurno.CANCELADO) throw BusinessException("No se puede confirmar un turno cancelado")
        estado        = EstadoTurno.CONFIRMADO
        actualizadoEn = LocalDateTime.now()
    }

    fun cancelar() {
        if (estado == EstadoTurno.COMPLETADO) throw BusinessException("No se puede cancelar un turno completado")
        estado        = EstadoTurno.CANCELADO
        actualizadoEn = LocalDateTime.now()
    }

    fun completar() {
        if (estado != EstadoTurno.CONFIRMADO) throw BusinessException("Solo se pueden completar turnos confirmados")
        estado        = EstadoTurno.COMPLETADO
        actualizadoEn = LocalDateTime.now()
    }

    fun estaActivo() = estado != EstadoTurno.CANCELADO && estado != EstadoTurno.COMPLETADO
}

//------------------------------------------

enum class EstadoTurno {
    PENDIENTE, CONFIRMADO, CANCELADO, COMPLETADO
}
