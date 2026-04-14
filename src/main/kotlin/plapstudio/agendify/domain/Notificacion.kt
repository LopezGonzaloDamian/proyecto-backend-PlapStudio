package plapstudio.agendify.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "notificaciones")
class Notificacion(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_usuario", nullable = false)
    val usuario: Usuario,

    var canal: String,
    var titulo: String,
    var cuerpo: String,
    var leida: Boolean = false,
    val enviadaEn: LocalDateTime = LocalDateTime.now(),
    var recursoTipo: String? = null,
    var recursoId: UUID? = null
)
