package plapstudio.agendify.repository

import plapstudio.agendify.domain.Agenda
import plapstudio.agendify.domain.Turno
import plapstudio.agendify.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TurnoRepository : JpaRepository<Turno, Long> {
    fun findByCliente(cliente: Usuario): List<Turno>
    fun findByAgenda(agenda: Agenda): List<Turno>
    fun existsByAgendaAndFechaHora(agenda: Agenda, fechaHora: LocalDateTime): Boolean
}
