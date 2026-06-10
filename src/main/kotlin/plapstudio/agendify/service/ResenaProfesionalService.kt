package plapstudio.agendify.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import plapstudio.agendify.domain.EstadoTurno
import plapstudio.agendify.domain.ResenaProfesional
import plapstudio.agendify.dto.ResenaCreateRequest
import plapstudio.agendify.errors.BusinessException
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.NotificacionRepository
import plapstudio.agendify.repository.PerfilClienteRepository
import plapstudio.agendify.repository.PerfilProfesionalRepository
import plapstudio.agendify.repository.ResenaProfesionalRepository
import plapstudio.agendify.repository.TurnoRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

@Service
class ResenaProfesionalService(
    private val resenaRepository: ResenaProfesionalRepository,
    private val turnoRepository: TurnoRepository,
    private val perfilClienteRepository: PerfilClienteRepository,
    private val perfilProfesionalRepository: PerfilProfesionalRepository,
    private val notificacionRepository: NotificacionRepository
) {
    private val zonaHorariaApp = ZoneId.of("America/Asuncion")

    fun findByProfesional(profesionalId: Long): List<ResenaProfesional> {
        val profesional = perfilProfesionalRepository.findById(profesionalId)
            .orElseThrow { NotFoundException("Perfil profesional no encontrado con id: $profesionalId") }
        return resenaRepository.findByProfesionalOrderByCreadaEnDesc(profesional)
    }

    @Transactional
    fun crear(clienteId: Long, req: ResenaCreateRequest): ResenaProfesional {
        val cliente = perfilClienteRepository.findById(clienteId)
            .orElseThrow { NotFoundException("Perfil cliente no encontrado con id: $clienteId") }
        val turno = turnoRepository.findById(req.turnoId)
            .orElseThrow { NotFoundException("Turno no encontrado con id: ${req.turnoId}") }

        if (turno.cliente?.id != cliente.id) {
            throw BusinessException("Solo el cliente del turno puede dejar una resena")
        }
        if (turno.estado != EstadoTurno.CONFIRMADO) {
            throw BusinessException("Solo se pueden calificar turnos confirmados")
        }
        if (LocalDateTime.now(zonaHorariaApp).isBefore(turno.iniciaEn.plusDays(3))) {
            throw BusinessException("La resena se habilita 3 dias despues del turno")
        }
        if (resenaRepository.existsByTurno(turno)) {
            throw BusinessException("Este turno ya fue calificado")
        }
        if (req.calificacion !in 1..5) {
            throw BusinessException("La calificacion debe estar entre 1 y 5")
        }

        val resena = resenaRepository.save(ResenaProfesional(
            profesional  = turno.agenda.profesional,
            cliente      = cliente,
            turno        = turno,
            calificacion = req.calificacion,
            comentario   = req.comentario.trim().take(600)
        ))

        notificacionRepository
            .findByUsuarioAndRecursoTipoAndRecursoId(cliente.usuario, "RESENA_TURNO", turno.id)
            .forEach { it.leida = true }

        return resena
    }
}
