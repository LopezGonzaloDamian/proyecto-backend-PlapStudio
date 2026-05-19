package plapstudio.agendify.controller

import org.springframework.web.bind.annotation.*
import plapstudio.agendify.dto.*
import plapstudio.agendify.service.TurnoService
import java.util.UUID

@RestController
@RequestMapping("/turnos")
@CrossOrigin("*")
class TurnoController(
    private val turnoService: TurnoService,
    private val mapper:       Mapper
) {

    @GetMapping
    fun getAll(): List<TurnoDto> =
        turnoService.findAll().map { mapper.toTurnoDto(it, turnoService.pagoDe(it)) }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): TurnoDto {
        val turno = turnoService.findById(id)
        return mapper.toTurnoDto(turno, turnoService.pagoDe(turno))
    }

    @GetMapping("/agenda/{agendaId}")
    fun getByAgenda(@PathVariable agendaId: UUID): List<TurnoDto> =
        turnoService.findByAgenda(agendaId).map { mapper.toTurnoDto(it, turnoService.pagoDe(it)) }

    @GetMapping("/cliente/{clienteId}")
    fun getByCliente(@PathVariable clienteId: Long): List<TurnoDto> =
        turnoService.findByCliente(clienteId).map { mapper.toTurnoDto(it, turnoService.pagoDe(it)) }

    @GetMapping("/profesional/{profesionalId}")
    fun getByProfesional(@PathVariable profesionalId: Long): List<TurnoDto> =
        turnoService.findByProfesional(profesionalId).map { mapper.toTurnoDto(it, turnoService.pagoDe(it)) }

    @PostMapping
    fun reservar(@RequestBody req: TurnoCreateRequest): TurnoDto {
        val turno = turnoService.reservar(req)
        return mapper.toTurnoDto(turno, turnoService.pagoDe(turno))
    }

    @PutMapping("/{id}")
    fun modificar(@PathVariable id: UUID, @RequestBody req: TurnoUpdateRequest): TurnoDto {
        val turno = turnoService.modificar(id, req)
        return mapper.toTurnoDto(turno, turnoService.pagoDe(turno))
    }

    @PatchMapping("/{id}/notas")
    fun actualizarNotas(@PathVariable id: UUID, @RequestBody req: TurnoNotasRequest): TurnoDto {
        val turno = turnoService.actualizarNotas(id, req.notas)
        return mapper.toTurnoDto(turno, turnoService.pagoDe(turno))
    }

    @PatchMapping("/{id}/cancelar")
    fun cancelar(@PathVariable id: UUID, @RequestBody(required = false) req: TurnoCancelRequest?): TurnoDto {
        val turno = turnoService.cancelar(id, req?.motivo)
        return mapper.toTurnoDto(turno, turnoService.pagoDe(turno))
    }
}
