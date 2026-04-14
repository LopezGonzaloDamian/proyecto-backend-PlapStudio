package plapstudio.agendify.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "historial_turnos")
class HistorialTurno(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id", nullable = false)
    val turno: Turno,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_modificado_por")
    val modificadoPor: Usuario? = null,

    @Enumerated(EnumType.STRING)
    val estadoAnterior: EstadoTurno,

    @Enumerated(EnumType.STRING)
    val estadoNuevo: EstadoTurno,

    val modificadoEn: LocalDateTime = LocalDateTime.now()
)
