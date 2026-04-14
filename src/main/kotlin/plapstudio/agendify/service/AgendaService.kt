package plapstudio.agendify.service

import plapstudio.agendify.domain.Agenda
import plapstudio.agendify.errors.BusinessException
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.AgendaRepository
import plapstudio.agendify.repository.PerfilProfesionalRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AgendaService(
    private val agendaRepository:            AgendaRepository,
    private val perfilProfesionalRepository: PerfilProfesionalRepository
) {

    fun findById(id: UUID): Agenda =
        agendaRepository.findById(id).orElseThrow { NotFoundException("Agenda no encontrada con id: $id") }

    fun findAll(): List<Agenda> = agendaRepository.findAll()

    fun findActivas(): List<Agenda> = agendaRepository.findByActivaTrue()

    fun findByProfesional(profesionalId: Long): List<Agenda> {
        val perfil = perfilProfesionalRepository.findById(profesionalId)
            .orElseThrow { NotFoundException("Perfil profesional no encontrado con id: $profesionalId") }
        return agendaRepository.findByProfesional(perfil)
    }

    fun create(agenda: Agenda): Agenda {
        if (!agenda.profesional.usuario.esProfesional()) {
            throw BusinessException("Solo un profesional puede crear una agenda")
        }
        return agendaRepository.save(agenda)
    }

    fun update(id: UUID, datos: Agenda): Agenda {
        val existente             = findById(id)
        existente.nombre          = datos.nombre
        existente.descripcion     = datos.descripcion
        existente.configuraciones = datos.configuraciones
        existente.excepciones     = datos.excepciones
        return agendaRepository.save(existente)
    }

    fun darDeBaja(id: UUID) {
        val agenda = findById(id)
        agenda.darDeBaja()
        agendaRepository.save(agenda)
    }
}
