package plapstudio.agendify.controller

import org.springframework.web.bind.annotation.*
import plapstudio.agendify.dto.AsistenteAsignacionDto
import plapstudio.agendify.dto.AsistenteAsignarRequest
import plapstudio.agendify.dto.Mapper
import plapstudio.agendify.dto.TurnoDto
import plapstudio.agendify.service.AsistenteService
import plapstudio.agendify.service.TurnoService
import java.util.UUID

@RestController
@RequestMapping("/asistentes")
@CrossOrigin("*")
class AsistenteController(
    private val service:      AsistenteService,
    private val turnoService: TurnoService,
    private val mapper:       Mapper
) {

    @GetMapping("/{usuarioId}/profesionales")
    fun profesionales(@PathVariable usuarioId: Long): List<AsistenteAsignacionDto> =
        service.profesionalesDe(usuarioId).map { mapper.toAsistenteAsignacionDto(it) }

    @GetMapping("/profesional/{profesionalId}")
    fun asistentesDe(@PathVariable profesionalId: Long): List<AsistenteAsignacionDto> =
        service.asistentesDe(profesionalId).map { mapper.toAsistenteAsignacionDto(it) }

    @GetMapping("/{usuarioId}/turnos")
    fun turnos(@PathVariable usuarioId: Long): List<TurnoDto> =
        service.turnosDeAsistente(usuarioId).map { mapper.toTurnoDto(it, turnoService.pagoDe(it)) }

    @PostMapping
    fun asignar(@RequestBody req: AsistenteAsignarRequest): AsistenteAsignacionDto =
        mapper.toAsistenteAsignacionDto(service.asignar(req.profesionalId, req.asistenteId))

    @DeleteMapping("/{id}")
    fun desasignar(@PathVariable id: UUID) = service.desasignar(id)
}
