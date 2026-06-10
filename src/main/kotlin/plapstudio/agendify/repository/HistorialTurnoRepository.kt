package plapstudio.agendify.repository

import plapstudio.agendify.domain.HistorialTurno
import plapstudio.agendify.domain.Turno
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HistorialTurnoRepository : JpaRepository<HistorialTurno, UUID> {
    fun findByTurno(turno: Turno): List<HistorialTurno>
}
