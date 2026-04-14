package plapstudio.agendify.controller

import plapstudio.agendify.domain.Turno
import plapstudio.agendify.service.TurnoService
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/turnos")
@CrossOrigin("*")
class TurnoController(private val turnoService: TurnoService) {

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): Turno = turnoService.findById(id)

    @GetMapping("/agenda/{agendaId}")
    fun getByAgenda(@PathVariable agendaId: UUID): List<Turno> =
        turnoService.findByAgenda(agendaId)

    @GetMapping("/cliente/{clienteId}")
    fun getByCliente(@PathVariable clienteId: Long): List<Turno> =
        turnoService.findByCliente(clienteId)

    @PostMapping
    fun reservar(@RequestBody turno: Turno): Turno = turnoService.reservar(turno)

    @PatchMapping("/{id}/confirmar")
    fun confirmar(@PathVariable id: UUID): Turno = turnoService.confirmar(id)

    @PatchMapping("/{id}/cancelar")
    fun cancelar(@PathVariable id: UUID): Turno = turnoService.cancelar(id)

    @PatchMapping("/{id}/completar")
    fun completar(@PathVariable id: UUID): Turno = turnoService.completar(id)
}
