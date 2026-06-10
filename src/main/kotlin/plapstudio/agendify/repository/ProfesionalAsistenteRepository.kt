package plapstudio.agendify.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import plapstudio.agendify.domain.EstadoAsignacionAsistente
import plapstudio.agendify.domain.PerfilProfesional
import plapstudio.agendify.domain.ProfesionalAsistente
import plapstudio.agendify.domain.Usuario
import java.util.UUID

@Repository
interface ProfesionalAsistenteRepository : JpaRepository<ProfesionalAsistente, UUID> {
    fun findByAsistente(asistente: Usuario): List<ProfesionalAsistente>
    fun findByAsistenteAndEstado(asistente: Usuario, estado: EstadoAsignacionAsistente): List<ProfesionalAsistente>
    fun findByProfesional(profesional: PerfilProfesional): List<ProfesionalAsistente>
    fun existsByProfesionalAndAsistente(profesional: PerfilProfesional, asistente: Usuario): Boolean
    fun existsByProfesionalAndAsistenteAndEstado(profesional: PerfilProfesional, asistente: Usuario, estado: EstadoAsignacionAsistente): Boolean
    fun findByProfesionalAndAsistente(profesional: PerfilProfesional, asistente: Usuario): ProfesionalAsistente?
}
