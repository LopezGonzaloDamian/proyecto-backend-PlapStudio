package plapstudio.agendify.repository

import plapstudio.agendify.domain.Agenda
import plapstudio.agendify.domain.PerfilProfesional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AgendaRepository : JpaRepository<Agenda, UUID> {
    fun findByProfesional(profesional: PerfilProfesional): List<Agenda>
    fun findByActivaTrue(): List<Agenda>
}
