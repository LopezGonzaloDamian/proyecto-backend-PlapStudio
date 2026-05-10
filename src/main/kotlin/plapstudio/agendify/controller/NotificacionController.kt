package plapstudio.agendify.controller

import org.springframework.web.bind.annotation.*
import plapstudio.agendify.dto.Mapper
import plapstudio.agendify.dto.NotificacionDto
import plapstudio.agendify.service.NotificacionService
import java.util.UUID

@RestController
@RequestMapping("/notificaciones")
@CrossOrigin("*")
class NotificacionController(
    private val service: NotificacionService,
    private val mapper:  Mapper
) {

    @GetMapping("/usuario/{usuarioId}")
    fun listar(@PathVariable usuarioId: Long): List<NotificacionDto> =
        service.findByUsuario(usuarioId).map { mapper.toNotificacionDto(it) }

    @PatchMapping("/{id}/leer")
    fun marcarLeida(@PathVariable id: UUID): NotificacionDto =
        mapper.toNotificacionDto(service.marcarLeida(id))

    @PatchMapping("/usuario/{usuarioId}/leer-todas")
    fun marcarTodasLeidas(@PathVariable usuarioId: Long) =
        service.marcarTodasLeidas(usuarioId)
}
