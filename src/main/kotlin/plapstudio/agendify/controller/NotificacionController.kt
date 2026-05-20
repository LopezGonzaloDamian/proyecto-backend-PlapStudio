package plapstudio.agendify.controller

import org.springframework.web.bind.annotation.*
import plapstudio.agendify.auth.AuthGuard
import plapstudio.agendify.dto.Mapper
import plapstudio.agendify.dto.NotificacionDto
import plapstudio.agendify.service.NotificacionService
import java.util.UUID

@RestController
@RequestMapping("/notificaciones")
@CrossOrigin("*")
class NotificacionController(
    private val service: NotificacionService,
    private val mapper:  Mapper,
    private val authGuard: AuthGuard
) {

    @GetMapping("/usuario/{usuarioId}")
    fun listar(@PathVariable usuarioId: Long): List<NotificacionDto> {
        authGuard.requireUser(usuarioId)
        return service.findByUsuario(usuarioId).map { mapper.toNotificacionDto(it) }
    }

    @PatchMapping("/{id}/leer")
    fun marcarLeida(@PathVariable id: UUID): NotificacionDto {
        authGuard.requireNotificationOwner(id)
        return mapper.toNotificacionDto(service.marcarLeida(id))
    }

    @PatchMapping("/usuario/{usuarioId}/leer-todas")
    fun marcarTodasLeidas(@PathVariable usuarioId: Long) {
        authGuard.requireUser(usuarioId)
        service.marcarTodasLeidas(usuarioId)
    }
}
