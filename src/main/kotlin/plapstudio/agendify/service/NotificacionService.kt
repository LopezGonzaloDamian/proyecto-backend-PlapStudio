package plapstudio.agendify.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import plapstudio.agendify.domain.Notificacion
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.NotificacionRepository
import plapstudio.agendify.repository.UsuarioRepository
import java.util.UUID

@Service
class NotificacionService(
    private val notificacionRepository: NotificacionRepository,
    private val usuarioRepository:      UsuarioRepository
) {

    fun findByUsuario(usuarioId: Long): List<Notificacion> {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { NotFoundException("Usuario no encontrado con id: $usuarioId") }
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
}
