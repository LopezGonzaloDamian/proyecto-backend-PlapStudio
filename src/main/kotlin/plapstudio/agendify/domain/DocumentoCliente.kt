package plapstudio.agendify.domain

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "documentos_cliente")
class DocumentoCliente(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_cliente", nullable = false)
    val cliente: PerfilCliente,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_profesional", nullable = false)
    val profesional: PerfilProfesional,

    var nombreArchivo: String,
    var urlArchivo: String,
    var tipoArchivo: String,
    val subidoEn: LocalDateTime = LocalDateTime.now()
)
