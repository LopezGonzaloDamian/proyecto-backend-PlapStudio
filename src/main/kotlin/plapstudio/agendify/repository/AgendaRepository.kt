package plapstudio.agendify.repository

import plapstudio.agendify.domain.Agenda
import plapstudio.agendify.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AgendaRepository : JpaRepository<Agenda, Long> {
    fun findByProfesional(profesional: Usuario): List<Agenda>
    fun findByActivaTrue(): List<Agenda>
}
