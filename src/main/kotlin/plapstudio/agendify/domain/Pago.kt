package plapstudio.agendify.domain

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "pagos")
class Pago(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id", nullable = false)
    val turno: Turno,

    var monto: BigDecimal,
    var moneda: String = "ARS",

    @Enumerated(EnumType.STRING)
    var estado: EstadoPago = EstadoPago.PENDIENTE,

    var referenciaProveedorMock: String? = null,
    var pagadoEn: LocalDateTime? = null
)

//------------------------------------------

enum class EstadoPago {
    PENDIENTE, APROBADO, RECHAZADO, REEMBOLSADO
}
