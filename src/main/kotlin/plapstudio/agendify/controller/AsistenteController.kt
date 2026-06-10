package plapstudio.agendify.controller

import org.springframework.web.bind.annotation.*
import plapstudio.agendify.auth.AuthGuard
import plapstudio.agendify.dto.AsistenteAsignacionDto
import plapstudio.agendify.dto.AsistenteAsignarRequest
import plapstudio.agendify.dto.Mapper
import plapstudio.agendify.dto.TurnoCancelRequest
import plapstudio.agendify.dto.TurnoCreateRequest
import plapstudio.agendify.dto.TurnoDto
import plapstudio.agendify.dto.TurnoNotasRequest
import plapstudio.agendify.dto.TurnoUpdateRequest
import plapstudio.agendify.service.AsistenteService
import plapstudio.agendify.service.TurnoService
import java.util.UUID

@RestController
@RequestMapping("/asistentes")
@CrossOrigin("*")
class AsistenteController(
    private val service:      AsistenteService,
    private val turnoService: TurnoService,
    private val mapper:       Mapper,
    private val authGuard: AuthGuard
) {

    @GetMapping("/{usuarioId}/profesionales")
    fun profesionales(@PathVariable usuarioId: Long): List<AsistenteAsignacionDto> {
        authGuard.requireUser(usuarioId)
        return service.profesionalesDe(usuarioId).map { mapper.toAsistenteAsignacionDto(it) }
    }

    @GetMapping("/profesional/{profesionalId}")
    fun asistentesDe(@PathVariable profesionalId: Long): List<AsistenteAsignacionDto> {
        authGuard.requireProfesional(profesionalId)
        return service.asistentesDe(profesionalId).map { mapper.toAsistenteAsignacionDto(it) }
    }

    @GetMapping("/{usuarioId}/turnos")
    fun turnos(@PathVariable usuarioId: Long): List<TurnoDto> {
        authGuard.requireUser(usuarioId)
        return service.turnosDeAsistente(usuarioId).map { mapper.toTurnoDto(it, turnoService.pagoDe(it)) }
    }

    @PostMapping("/{usuarioId}/turnos")
    fun reservarTurno(@PathVariable usuarioId: Long, @RequestBody req: TurnoCreateRequest): TurnoDto {
        authGuard.requireUser(usuarioId)
        val turno = service.reservarTurno(usuarioId, req)
        return mapper.toTurnoDto(turno, turnoService.pagoDe(turno))
    }

    @PutMapping("/{usuarioId}/turnos/{turnoId}")
    fun modificarTurno(
        @PathVariable usuarioId: Long,
        @PathVariable turnoId: UUID,
        @RequestBody req: TurnoUpdateRequest
    ): TurnoDto {
        authGuard.requireUser(usuarioId)
        val turno = service.modificarTurno(usuarioId, turnoId, req)
        return mapper.toTurnoDto(turno, turnoService.pagoDe(turno))
    }

    @PatchMapping("/{usuarioId}/turnos/{turnoId}/notas")
    fun actualizarNotasTurno(
        @PathVariable usuarioId: Long,
        @PathVariable turnoId: UUID,
        @RequestBody req: TurnoNotasRequest
    ): TurnoDto {
        authGuard.requireUser(usuarioId)
        val turno = service.actualizarNotasTurno(usuarioId, turnoId, req.notas)
        return mapper.toTurnoDto(turno, turnoService.pagoDe(turno))
    }

    @PatchMapping("/{usuarioId}/turnos/{turnoId}/cancelar")
    fun cancelarTurno(
        @PathVariable usuarioId: Long,
        @PathVariable turnoId: UUID,
        @RequestBody(required = false) req: TurnoCancelRequest?
    ): TurnoDto {
        authGuard.requireUser(usuarioId)
        val turno = service.cancelarTurno(usuarioId, turnoId, req?.motivo)
        return mapper.toTurnoDto(turno, turnoService.pagoDe(turno))
    }

    @PostMapping
    fun asignar(@RequestBody req: AsistenteAsignarRequest): AsistenteAsignacionDto {
        authGuard.requireProfesional(req.profesionalId)
        return mapper.toAsistenteAsignacionDto(service.asignar(req.profesionalId, req.asistenteEmail))
    }

    @PatchMapping("/{usuarioId}/profesionales/{asignacionId}/aceptar")
    fun aceptar(
        @PathVariable usuarioId: Long,
        @PathVariable asignacionId: UUID
    ): AsistenteAsignacionDto {
        authGuard.requireUser(usuarioId)
        return mapper.toAsistenteAsignacionDto(service.aceptar(usuarioId, asignacionId))
    }

    @PatchMapping("/{usuarioId}/profesionales/{asignacionId}/rechazar")
    fun rechazar(
        @PathVariable usuarioId: Long,
        @PathVariable asignacionId: UUID
    ): AsistenteAsignacionDto {
        authGuard.requireUser(usuarioId)
        return mapper.toAsistenteAsignacionDto(service.rechazar(usuarioId, asignacionId))
    }

    @DeleteMapping("/{id}")
    fun desasignar(@PathVariable id: UUID) {
        authGuard.requireAssignmentManager(id)
        service.desasignar(id)
    }
}
