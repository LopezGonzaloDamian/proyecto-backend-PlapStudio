package plapstudio.agendify.domain

import jakarta.persistence.*
import java.math.BigDecimal
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
    @JoinColumn(name = "email_cliente", nullable = true)
    val cliente: PerfilCliente? = null,

    var clienteExternoNombre: String? = null,
    var clienteExternoTelefono: String? = null,
    var clienteExternoDni: String? = null,
    var clienteExternoEmail: String? = null,

    var iniciaEn: LocalDateTime,
    var duracionMinutos: Int,

    @Enumerated(EnumType.STRING)
    var estado: EstadoTurno = EstadoTurno.CONFIRMADO,

    @Column(precision = 12, scale = 2)
    var precio: BigDecimal = BigDecimal.ZERO,

    var notas: String = "",
    val creadoEn: LocalDateTime = LocalDateTime.now(),
    var actualizadoEn: LocalDateTime = LocalDateTime.now()
) {
    fun cancelar() {
        estado        = EstadoTurno.CANCELADO
        actualizadoEn = LocalDateTime.now()
    }

    fun estaActivo() = estado != EstadoTurno.CANCELADO
}

//------------------------------------------

enum class EstadoTurno {
    CONFIRMADO, CANCELADO
}
