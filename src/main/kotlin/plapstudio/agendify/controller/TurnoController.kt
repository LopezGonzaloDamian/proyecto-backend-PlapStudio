package plapstudio.agendify.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import plapstudio.agendify.auth.AuthGuard
import plapstudio.agendify.dto.*
import plapstudio.agendify.service.TurnoService
import java.util.UUID

@RestController
@RequestMapping("/turnos")
@CrossOrigin("*")
class TurnoController(
    private val turnoService: TurnoService,
    private val mapper:       Mapper,
    private val authGuard: AuthGuard
) {

    @GetMapping
    fun getAll(): List<TurnoDto> {
        authGuard.requireAdmin()
        return turnoService.findAll().map { mapper.toTurnoDto(it, turnoService.pagoDe(it)) }
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): TurnoDto {
        authGuard.requireTurnoParticipant(id)
        val turno = turnoService.findById(id)
        return mapper.toTurnoDto(turno, turnoService.pagoDe(turno))
    }

    @GetMapping("/agenda/{agendaId}")
    fun getByAgenda(@PathVariable agendaId: UUID): List<TurnoDto> {
        authGuard.requireAgendaOwner(agendaId)
        return turnoService.findByAgenda(agendaId).map { mapper.toTurnoDto(it, turnoService.pagoDe(it)) }
    }

    @GetMapping("/cliente/{clienteId}")
    fun getByCliente(@PathVariable clienteId: Long): List<TurnoDto> {
        authGuard.requireCliente(clienteId)
        return turnoService.findByCliente(clienteId).map { mapper.toTurnoDto(it, turnoService.pagoDe(it)) }
    }

    @GetMapping("/profesional/{profesionalId}")
    fun getByProfesional(@PathVariable profesionalId: Long): List<TurnoDto> {
        authGuard.requireProfesionalOrAssignedAssistant(profesionalId)
        return turnoService.findByProfesional(profesionalId).map { mapper.toTurnoDto(it, turnoService.pagoDe(it)) }
    }

    @PostMapping
    fun reservar(@Valid @RequestBody req: TurnoCreateRequest): TurnoDto {
        if (req.clienteId != null) {
            val usuario = authGuard.requireAuthenticated()
            if (!usuario.esAdmin() && usuario.perfilCliente?.id != req.clienteId) {
                authGuard.requireAgendaStaff(req.agendaId)
            }
        } else {
            authGuard.requireAgendaStaff(req.agendaId)
        }
        val turno = turnoService.reservar(req)
        return mapper.toTurnoDto(turno, turnoService.pagoDe(turno))
    }

    @PutMapping("/{id}")
    fun modificar(@PathVariable id: UUID, @Valid @RequestBody req: TurnoUpdateRequest): TurnoDto {
        authGuard.requireTurnoStaff(id)
        val turno = turnoService.modificar(id, req)
        return mapper.toTurnoDto(turno, turnoService.pagoDe(turno))
    }

    @PatchMapping("/{id}/notas")
    fun actualizarNotas(@PathVariable id: UUID, @Valid @RequestBody req: TurnoNotasRequest): TurnoDto {
        authGuard.requireTurnoStaff(id)
        val turno = turnoService.actualizarNotas(id, req.notas)
        return mapper.toTurnoDto(turno, turnoService.pagoDe(turno))
    }

    @PatchMapping("/{id}/cancelar")
    fun cancelar(@PathVariable id: UUID, @Valid @RequestBody(required = false) req: TurnoCancelRequest?): TurnoDto {
        authGuard.requireTurnoParticipant(id)
        val turno = turnoService.cancelar(id, req?.motivo)
        return mapper.toTurnoDto(turno, turnoService.pagoDe(turno))
    }
}
