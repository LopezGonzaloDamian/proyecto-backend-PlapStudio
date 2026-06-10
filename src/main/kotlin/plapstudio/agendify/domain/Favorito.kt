package plapstudio.agendify.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "favoritos",
    uniqueConstraints = [UniqueConstraint(columnNames = ["email_cliente", "email_profesional"])]
)
class Favorito(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_cliente", nullable = false)
    val cliente: PerfilCliente,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_profesional", nullable = false)
    val profesional: PerfilProfesional,

    val agregadoEn: LocalDateTime = LocalDateTime.now()
)
