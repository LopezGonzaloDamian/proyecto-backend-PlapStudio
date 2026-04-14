package plapstudio.agendify.service

import plapstudio.agendify.domain.Turno
import plapstudio.agendify.errors.BusinessException
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.TurnoRepository
import org.springframework.stereotype.Service

@Service
class TurnoService(
    private val turnoRepository: TurnoRepository,
    private val agendaService:   AgendaService,
    private val usuarioService:  UsuarioService
) {

    fun findById(id: Long): Turno =
        turnoRepository.findById(id).orElseThrow { NotFoundException("Turno no encontrado con id: $id") }

    fun findByAgenda(agendaId: Long): List<Turno> {
        val agenda = agendaService.findById(agendaId)
        return turnoRepository.findByAgenda(agenda)
    }

    fun findByCliente(clienteId: Long): List<Turno> {
        val cliente = usuarioService.findById(clienteId)
        return turnoRepository.findByCliente(cliente)
    }

    fun reservar(turno: Turno): Turno {
        if (!turno.cliente.esCliente()) {
            throw BusinessException("Solo un cliente puede reservar un turno")
        }
        if (!turno.agenda.estaDisponibleEn(turno.fechaHora.dayOfWeek, turno.fechaHora.toLocalTime())) {
            throw BusinessException("La agenda no está disponible en el horario solicitado")
        }
        if (turnoRepository.existsByAgendaAndFechaHora(turno.agenda, turno.fechaHora)) {
            throw BusinessException("Ya existe un turno reservado en esa fecha y horario")
        }
        return turnoRepository.save(turno)
    }

    fun cancelar(id: Long): Turno {
        val turno = findById(id)
        turno.cancelar()
        return turnoRepository.save(turno)
    }

    fun confirmar(id: Long): Turno {
        val turno = findById(id)
        turno.confirmar()
        return turnoRepository.save(turno)
    }

    fun completar(id: Long): Turno {
        val turno = findById(id)
        turno.completar()
        return turnoRepository.save(turno)
    }
}
