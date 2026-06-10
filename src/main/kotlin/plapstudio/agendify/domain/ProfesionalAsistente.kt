package plapstudio.agendify.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

enum class EstadoAsignacionAsistente {
    PENDIENTE,
    ACEPTADA,
    RECHAZADA
}

@Entity
@Table(
    name = "profesional_asistente",
    uniqueConstraints = [UniqueConstraint(columnNames = ["email_profesional", "email_asistente"])]
)
class ProfesionalAsistente(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "email_profesional", nullable = false)
    val profesional: PerfilProfesional,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "email_asistente", nullable = false)
    val asistente: Usuario,

    @Enumerated(EnumType.STRING)
    var estado: EstadoAsignacionAsistente = EstadoAsignacionAsistente.PENDIENTE,

    val asignadoEn: LocalDateTime = LocalDateTime.now()
)
