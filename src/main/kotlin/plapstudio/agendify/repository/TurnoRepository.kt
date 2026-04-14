package plapstudio.agendify.repository

import plapstudio.agendify.domain.Agenda
import plapstudio.agendify.domain.PerfilCliente
import plapstudio.agendify.domain.Turno
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface TurnoRepository : JpaRepository<Turno, UUID> {
    fun findByCliente(cliente: PerfilCliente): List<Turno>
    fun findByAgenda(agenda: Agenda): List<Turno>
    fun existsByAgendaAndIniciaEn(agenda: Agenda, iniciaEn: LocalDateTime): Boolean
}
