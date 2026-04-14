package plapstudio.agendify.domain

import plapstudio.agendify.errors.BusinessException
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "turnos")
class Turno(
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agenda_id", nullable = false)
    val agenda: Agenda,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cliente_id", nullable = false)
    val cliente: Usuario,

    var fechaHora: LocalDateTime,

    @Enumerated(EnumType.STRING)
    var estado: EstadoTurno = EstadoTurno.PENDIENTE,

    var pagado: Boolean = false
) : Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Long? = null

    fun confirmar() {
        if (estado == EstadoTurno.CANCELADO) throw BusinessException("No se puede confirmar un turno cancelado")
        estado = EstadoTurno.CONFIRMADO
    }

    fun cancelar() {
        if (estado == EstadoTurno.COMPLETADO) throw BusinessException("No se puede cancelar un turno completado")
        estado = EstadoTurno.CANCELADO
    }

    fun completar() {
        if (estado != EstadoTurno.CONFIRMADO) throw BusinessException("Solo se pueden completar turnos confirmados")
        estado = EstadoTurno.COMPLETADO
    }

    fun estaActivo() = estado != EstadoTurno.CANCELADO && estado != EstadoTurno.COMPLETADO
}

//------------------------------------------

enum class EstadoTurno {
    PENDIENTE, CONFIRMADO, CANCELADO, COMPLETADO
}
