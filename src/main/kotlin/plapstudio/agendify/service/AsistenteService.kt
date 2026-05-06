package plapstudio.agendify.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import plapstudio.agendify.domain.ProfesionalAsistente
import plapstudio.agendify.domain.Turno
import plapstudio.agendify.errors.BusinessException
import plapstudio.agendify.errors.ConflictException
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.PerfilProfesionalRepository
import plapstudio.agendify.repository.ProfesionalAsistenteRepository
import plapstudio.agendify.repository.TurnoRepository
import plapstudio.agendify.repository.UsuarioRepository
import java.util.UUID

@Service
class AsistenteService(
    private val profesionalAsistenteRepository: ProfesionalAsistenteRepository,
    private val perfilProfesionalRepository:    PerfilProfesionalRepository,
    private val usuarioRepository:              UsuarioRepository,
    private val turnoRepository:                TurnoRepository
) {

    fun profesionalesDe(asistenteId: Long): List<ProfesionalAsistente> {
        val asistente = usuarioRepository.findById(asistenteId)
            .orElseThrow { NotFoundException("Usuario asistente no encontrado con id: $asistenteId") }
        return profesionalAsistenteRepository.findByAsistente(asistente)
    }

    fun asistentesDe(profesionalId: Long): List<ProfesionalAsistente> {
        val perfil = perfilProfesionalRepository.findById(profesionalId)
            .orElseThrow { NotFoundException("Profesional no encontrado con id: $profesionalId") }
        return profesionalAsistenteRepository.findByProfesional(perfil)
    }

    fun turnosDeAsistente(asistenteId: Long): List<Turno> {
        val asignaciones = profesionalesDe(asistenteId)
        return asignaciones.flatMap { turnoRepository.findByAgendaProfesional(it.profesional) }
    }

    @Transactional
    fun asignar(profesionalId: Long, asistenteId: Long): ProfesionalAsistente {
        val profesional = perfilProfesionalRepository.findById(profesionalId)
            .orElseThrow { NotFoundException("Profesional no encontrado con id: $profesionalId") }
        val asistente = usuarioRepository.findById(asistenteId)
            .orElseThrow { NotFoundException("Usuario asistente no encontrado con id: $asistenteId") }
        if (!asistente.esAsistente()) {
            throw BusinessException("El usuario no tiene rol ASISTENTE")
        }
        if (profesionalAsistenteRepository.existsByProfesionalAndAsistente(profesional, asistente)) {
            throw ConflictException("Ya existe la asignación")
        }
        return profesionalAsistenteRepository.save(
            ProfesionalAsistente(profesional = profesional, asistente = asistente)
        )
    }

    @Transactional
    fun desasignar(id: UUID) {
        val asignacion = profesionalAsistenteRepository.findById(id)
            .orElseThrow { NotFoundException("Asignación no encontrada con id: $id") }
        profesionalAsistenteRepository.delete(asignacion)
    }
}
