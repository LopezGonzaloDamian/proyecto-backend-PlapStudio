package plapstudio.agendify.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import plapstudio.agendify.domain.EstadoTurno
import plapstudio.agendify.domain.Notificacion
import plapstudio.agendify.domain.Usuario
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.NotificacionRepository
import plapstudio.agendify.repository.ResenaProfesionalRepository
import plapstudio.agendify.repository.TurnoRepository
import plapstudio.agendify.repository.UsuarioRepository
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

@Service
class NotificacionService(
    private val notificacionRepository: NotificacionRepository,
    private val usuarioRepository:      UsuarioRepository,
    private val turnoRepository:        TurnoRepository,
    private val resenaRepository:       ResenaProfesionalRepository
) {
    private val zonaHorariaApp = ZoneId.of("America/Asuncion")

    @Transactional
    fun findByUsuario(usuarioId: Long): List<Notificacion> {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { NotFoundException("Usuario no encontrado con id: $usuarioId") }
        generarNotificacionesDeResena(usuario)
        return notificacionRepository.findByUsuarioOrderByEnviadaEnDesc(usuario)
    }

    @Transactional
    fun marcarLeida(id: UUID): Notificacion {
        val n = notificacionRepository.findById(id)
            .orElseThrow { NotFoundException("Notificación no encontrada con id: $id") }
        n.leida = true
        return notificacionRepository.save(n)
    }

    @Transactional
    fun marcarTodasLeidas(usuarioId: Long) {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { NotFoundException("Usuario no encontrado con id: $usuarioId") }
        val noLeidas = notificacionRepository.findByUsuarioAndLeidaFalse(usuario)
        noLeidas.forEach { it.leida = true }
        notificacionRepository.saveAll(noLeidas)
    }

    private fun generarNotificacionesDeResena(usuario: Usuario) {
        val cliente = usuario.perfilCliente ?: return
        val limite = LocalDateTime.now(zonaHorariaApp).minusDays(3)
        turnoRepository.findByClienteAndEstadoAndIniciaEnBefore(cliente, EstadoTurno.CONFIRMADO, limite)
            .asSequence()
            .filter { !resenaRepository.existsByTurno(it) }
            .filter { !notificacionRepository.existsByUsuarioAndRecursoTipoAndRecursoId(usuario, "RESENA_TURNO", it.id) }
            .forEach { turno ->
                notificacionRepository.save(Notificacion(
                    usuario     = usuario,
                    canal       = "IN_APP",
                    titulo      = "Califica tu atencion",
                    cuerpo      = "Ya podes calificar tu turno con ${turno.agenda.profesional.usuario.nombreCompleto}.",
                    recursoTipo = "RESENA_TURNO",
                    recursoId   = turno.id
                ))
            }
    }
}
