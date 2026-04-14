package plapstudio.agendify.repository

import plapstudio.agendify.domain.Pago
import plapstudio.agendify.domain.Turno
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PagoRepository : JpaRepository<Pago, UUID> {
    fun findByTurno(turno: Turno): Pago?
}
