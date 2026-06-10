package plapstudio.agendify.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "resenas_profesional")
class ResenaProfesional(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profesional_id", nullable = false)
    val profesional: PerfilProfesional,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    val cliente: PerfilCliente,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id", nullable = false, unique = true)
    val turno: Turno,

    var calificacion: Int,

    @Column(length = 600)
    var comentario: String = "",

    val creadaEn: LocalDateTime = LocalDateTime.now()
)
