package plapstudio.agendify.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import plapstudio.agendify.domain.PerfilProfesional
import plapstudio.agendify.domain.ResenaProfesional
import plapstudio.agendify.domain.Turno
import java.util.UUID

@Repository
interface ResenaProfesionalRepository : JpaRepository<ResenaProfesional, UUID> {
    fun findByProfesionalOrderByCreadaEnDesc(profesional: PerfilProfesional): List<ResenaProfesional>
    fun existsByTurno(turno: Turno): Boolean
}
