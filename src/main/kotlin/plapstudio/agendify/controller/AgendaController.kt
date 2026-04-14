package plapstudio.agendify.controller

import plapstudio.agendify.domain.Agenda
import plapstudio.agendify.service.AgendaService
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/agendas")
@CrossOrigin("*")
class AgendaController(private val agendaService: AgendaService) {

    @GetMapping
    fun getAll(): List<Agenda> = agendaService.findAll()

    @GetMapping("/activas")
    fun getActivas(): List<Agenda> = agendaService.findActivas()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): Agenda = agendaService.findById(id)

    @GetMapping("/profesional/{profesionalId}")
    fun getByProfesional(@PathVariable profesionalId: Long): List<Agenda> =
        agendaService.findByProfesional(profesionalId)

    @PostMapping
    fun create(@RequestBody agenda: Agenda): Agenda = agendaService.create(agenda)

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody datos: Agenda): Agenda =
        agendaService.update(id, datos)

    @DeleteMapping("/{id}")
    fun darDeBaja(@PathVariable id: UUID) = agendaService.darDeBaja(id)
}
