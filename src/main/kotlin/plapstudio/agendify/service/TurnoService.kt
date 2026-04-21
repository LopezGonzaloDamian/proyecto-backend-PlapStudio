package plapstudio.agendify.service

import plapstudio.agendify.domain.EstadoTurno
import plapstudio.agendify.domain.HistorialTurno
import plapstudio.agendify.domain.Turno
import plapstudio.agendify.errors.BusinessException
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.HistorialTurnoRepository
import plapstudio.agendify.repository.PerfilClienteRepository
import plapstudio.agendify.repository.TurnoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TurnoService(
    private val turnoRepository:          TurnoRepository,
    private val historialTurnoRepository: HistorialTurnoRepository,
    private val agendaService:            AgendaService,
    private val perfilClienteRepository:  PerfilClienteRepository,
    private val n8nWebhookService:        N8nWebhookService
) {

    fun findById(id: UUID): Turno =
        turnoRepository.findById(id).orElseThrow { NotFoundException("Turno no encontrado con id: $id") }

    fun findByAgenda(agendaId: UUID): List<Turno> {
        val agenda = agendaService.findById(agendaId)
        return turnoRepository.findByAgenda(agenda)
    }

    fun findByCliente(clienteId: Long): List<Turno> {
        val perfil = perfilClienteRepository.findById(clienteId)
            .orElseThrow { NotFoundException("Perfil cliente no encontrado con id: $clienteId") }
        return turnoRepository.findByCliente(perfil)
    }

    fun reservar(turno: Turno): Turno {
        if (!turno.cliente.usuario.esCliente()) {
            throw BusinessException("Solo un cliente puede reservar un turno")
        }
        val fecha = turno.iniciaEn.toLocalDate()
        val hora  = turno.iniciaEn.toLocalTime()
        if (turno.agenda.tieneExcepcionEn(fecha)) {
            throw BusinessException("La agenda no está disponible en esa fecha")
        }
        if (!turno.agenda.estaDisponibleEn(turno.iniciaEn.dayOfWeek, hora, turno.duracionMinutos)) {
            throw BusinessException("La agenda no está disponible en el horario solicitado")
        }
        if (turnoRepository.existsByAgendaAndIniciaEn(turno.agenda, turno.iniciaEn)) {
            throw BusinessException("Ya existe un turno en esa fecha y horario")
        }
        return turnoRepository.save(turno)
    }

    fun cancelar(id: UUID): Turno  = cambiarEstado(id) { it.cancelar() }

    @Transactional
    fun confirmar(id: UUID): Turno {
        val turnoConfirmado = cambiarEstado(id) { it.confirmar() }
        n8nWebhookService.notificarTurnoConfirmado(turnoConfirmado)
        return turnoConfirmado
    }

    fun completar(id: UUID): Turno = cambiarEstado(id) { it.completar() }

    private fun cambiarEstado(id: UUID, accion: (Turno) -> Unit): Turno {
        val turno          = findById(id)
        val estadoAnterior = turno.estado
        accion(turno)
        val saved = turnoRepository.save(turno)
        historialTurnoRepository.save(
            HistorialTurno(
                turno          = saved,
                estadoAnterior = estadoAnterior,
                estadoNuevo    = saved.estado
            )
        )
        return saved
    }
}
