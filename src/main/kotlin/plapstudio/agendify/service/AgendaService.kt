package plapstudio.agendify.service

import plapstudio.agendify.domain.Agenda
import plapstudio.agendify.errors.BusinessException
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.AgendaRepository
import org.springframework.stereotype.Service

@Service
class AgendaService(
    private val agendaRepository: AgendaRepository,
    private val usuarioService:   UsuarioService
) {

    fun findById(id: Long): Agenda =
        agendaRepository.findById(id).orElseThrow { NotFoundException("Agenda no encontrada con id: $id") }

    fun findAll(): List<Agenda> = agendaRepository.findAll()

    fun findActivas(): List<Agenda> = agendaRepository.findByActivaTrue()

    fun findByProfesional(profesionalId: Long): List<Agenda> {
        val profesional = usuarioService.findById(profesionalId)
        return agendaRepository.findByProfesional(profesional)
    }

    fun create(agenda: Agenda): Agenda {
        if (!agenda.profesional.esProfesional()) {
            throw BusinessException("Solo un profesional puede crear una agenda")
        }
        if (agenda.horaInicio >= agenda.horaFin) {
            throw BusinessException("La hora de inicio debe ser anterior a la hora de fin")
        }
        if (agenda.duracionTurnoMinutos <= 0) {
            throw BusinessException("La duración del turno debe ser positiva")
        }
        return agendaRepository.save(agenda)
    }

    fun update(id: Long, datos: Agenda): Agenda {
        val existente                  = findById(id)
        existente.nombre               = datos.nombre
        existente.descripcion          = datos.descripcion
        existente.diasDisponibles      = datos.diasDisponibles
        existente.horaInicio           = datos.horaInicio
        existente.horaFin              = datos.horaFin
        existente.duracionTurnoMinutos = datos.duracionTurnoMinutos
        return agendaRepository.save(existente)
    }

    fun darDeBaja(id: Long) {
        val agenda = findById(id)
        agenda.darDeBaja()
        agendaRepository.save(agenda)
    }
}
