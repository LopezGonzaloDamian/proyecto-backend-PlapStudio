package plapstudio.agendify.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "facturas")
class Factura(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pago_id", nullable = false)
    val pago: Pago,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_cliente", nullable = false)
    val cliente: PerfilCliente,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "email_profesional", nullable = false)
    val profesional: PerfilProfesional,

    val numeroFactura: String,
    var montoTotal: BigDecimal,
    var moneda: String = "ARS",

    @Enumerated(EnumType.STRING)
    var estado: EstadoFactura = EstadoFactura.EMITIDA,

    var urlPdfMock: String? = null,
    val emitidaEn: LocalDateTime = LocalDateTime.now()
)

//------------------------------------------

enum class EstadoFactura {
    EMITIDA, ANULADA
}
