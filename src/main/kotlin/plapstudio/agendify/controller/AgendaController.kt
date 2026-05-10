package plapstudio.agendify.controller

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import plapstudio.agendify.dto.*
import plapstudio.agendify.service.AgendaService
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/agendas")
@CrossOrigin("*")
class AgendaController(
    private val service: AgendaService,
    private val mapper:  Mapper
) {

    @GetMapping
    fun getAll(): List<AgendaDto> = service.findAll().map { mapper.toAgendaDto(it) }

    @GetMapping("/activas")
    fun getActivas(): List<AgendaDto> = service.findActivas().map { mapper.toAgendaDto(it) }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): AgendaDto = mapper.toAgendaDto(service.findById(id))

    @GetMapping("/profesional/{profesionalId}")
    fun getByProfesional(@PathVariable profesionalId: Long): List<AgendaDto> =
        service.findByProfesional(profesionalId).map { mapper.toAgendaDto(it) }

    @PostMapping
    fun create(@RequestBody req: AgendaCreateRequest): AgendaDto =
        mapper.toAgendaDto(service.create(req))

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody req: AgendaUpdateRequest): AgendaDto =
        mapper.toAgendaDto(service.update(id, req))

    @DeleteMapping("/{id}")
    fun darDeBaja(@PathVariable id: UUID) = service.darDeBaja(id)

    // ── Configuración horaria ─────────────────────────────────────────────────

    @PutMapping("/{id}/configuraciones")
    fun reemplazarConfiguraciones(
        @PathVariable id: UUID,
        @RequestBody items: List<ConfiguracionHorariaDto>
    ): AgendaDto = mapper.toAgendaDto(service.reemplazarConfiguraciones(id, items))

    @PostMapping("/{id}/configuraciones")
    fun agregarConfiguracion(
        @PathVariable id: UUID,
        @RequestBody dto: ConfiguracionHorariaDto
    ): AgendaDto = mapper.toAgendaDto(service.agregarConfiguracion(id, dto))

    @DeleteMapping("/{id}/configuraciones/{configId}")
    fun eliminarConfiguracion(
        @PathVariable id: UUID,
        @PathVariable configId: UUID
    ): AgendaDto = mapper.toAgendaDto(service.eliminarConfiguracion(id, configId))

    // ── Excepciones ───────────────────────────────────────────────────────────

    @PostMapping("/{id}/excepciones")
    fun agregarExcepcion(
        @PathVariable id: UUID,
        @RequestBody dto: ExcepcionAgendaDto
    ): AgendaDto = mapper.toAgendaDto(service.agregarExcepcion(id, dto))

    @DeleteMapping("/{id}/excepciones/{excepcionId}")
    fun eliminarExcepcion(
        @PathVariable id: UUID,
        @PathVariable excepcionId: UUID
    ): AgendaDto = mapper.toAgendaDto(service.eliminarExcepcion(id, excepcionId))

    // ── Slots disponibles ─────────────────────────────────────────────────────

    @GetMapping("/{id}/slots")
    fun slotsDisponibles(
        @PathVariable id: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fecha: LocalDate
    ): List<SlotDto> = service.slotsDisponibles(id, fecha)
}
