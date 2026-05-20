package plapstudio.agendify.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import plapstudio.agendify.domain.PerfilProfesional
import plapstudio.agendify.domain.ProfesionalAsistente
import plapstudio.agendify.domain.Turno
import plapstudio.agendify.dto.TurnoCreateRequest
import plapstudio.agendify.dto.TurnoUpdateRequest
import plapstudio.agendify.errors.BusinessException
import plapstudio.agendify.errors.ConflictException
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.AgendaRepository
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
    private val turnoRepository:                TurnoRepository,
    private val agendaRepository:               AgendaRepository,
    private val turnoService:                   TurnoService
) {

    fun profesionalesDe(asistenteId: Long): List<ProfesionalAsistente> {
        val asistente = usuarioRepository.findById(asistenteId)
            .orElseThrow { NotFoundException("Usuario asistente no encontrado con id: $asistenteId") }
        if (!asistente.esAsistente()) {
            throw BusinessException("El usuario no tiene rol ASISTENTE")
        }
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
    fun reservarTurno(asistenteId: Long, req: TurnoCreateRequest): Turno {
        val agenda = agendaRepository.findById(req.agendaId)
            .orElseThrow { NotFoundException("Agenda no encontrada con id: ${req.agendaId}") }
        validarProfesionalAsignado(asistenteId, agenda.profesional)
        return turnoService.reservar(req)
    }

    @Transactional
    fun modificarTurno(asistenteId: Long, turnoId: UUID, req: TurnoUpdateRequest): Turno {
        val turno = turnoService.findById(turnoId)
        validarProfesionalAsignado(asistenteId, turno.agenda.profesional)
        return turnoService.modificar(turnoId, req)
    }

    @Transactional
    fun actualizarNotasTurno(asistenteId: Long, turnoId: UUID, notas: String): Turno {
        val turno = turnoService.findById(turnoId)
        validarProfesionalAsignado(asistenteId, turno.agenda.profesional)
        return turnoService.actualizarNotas(turnoId, notas)
    }

    @Transactional
    fun cancelarTurno(asistenteId: Long, turnoId: UUID, motivo: String? = null): Turno {
        val turno = turnoService.findById(turnoId)
        validarProfesionalAsignado(asistenteId, turno.agenda.profesional)
        return turnoService.cancelar(turnoId, motivo)
    }

    @Transactional
    fun asignar(profesionalId: Long, asistenteEmail: String): ProfesionalAsistente {
        val profesional = perfilProfesionalRepository.findById(profesionalId)
            .orElseThrow { NotFoundException("Profesional no encontrado con id: $profesionalId") }
        val asistente = usuarioRepository.findByEmail(asistenteEmail.trim())
            ?: throw NotFoundException("No encontramos un asistente registrado con ese email")
        if (!asistente.esAsistente()) {
            throw BusinessException("El email ingresado no corresponde a un usuario con rol ASISTENTE")
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

    private fun validarProfesionalAsignado(asistenteId: Long, profesional: PerfilProfesional) {
        val asignado = profesionalesDe(asistenteId).any { it.profesional.id == profesional.id }
        if (!asignado) {
            throw BusinessException("El asistente no tiene asignada esta agenda")
        }
    }
}
