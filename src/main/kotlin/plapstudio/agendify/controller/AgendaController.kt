package plapstudio.agendify.controller

import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import plapstudio.agendify.auth.AuthGuard
import plapstudio.agendify.dto.*
import plapstudio.agendify.service.AgendaService
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/agendas")
@CrossOrigin("*")
class AgendaController(
    private val service: AgendaService,
    private val mapper:  Mapper,
    private val authGuard: AuthGuard
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
    fun create(@RequestBody req: AgendaCreateRequest): AgendaDto {
        authGuard.requireProfesional(req.profesionalId)
        return mapper.toAgendaDto(service.create(req))
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @RequestBody req: AgendaUpdateRequest): AgendaDto {
        authGuard.requireAgendaOwner(id)
        return mapper.toAgendaDto(service.update(id, req))
    }

    @DeleteMapping("/{id}")
    fun darDeBaja(@PathVariable id: UUID) {
        authGuard.requireAgendaOwner(id)
        service.darDeBaja(id)
    }

    // ── Configuración horaria ─────────────────────────────────────────────────

    @PutMapping("/{id}/configuraciones")
    fun reemplazarConfiguraciones(
        @PathVariable id: UUID,
        @RequestBody items: List<ConfiguracionHorariaDto>
    ): AgendaDto {
        authGuard.requireAgendaOwner(id)
        return mapper.toAgendaDto(service.reemplazarConfiguraciones(id, items))
    }

    @PostMapping("/{id}/configuraciones")
    fun agregarConfiguracion(
        @PathVariable id: UUID,
        @RequestBody dto: ConfiguracionHorariaDto
    ): AgendaDto {
        authGuard.requireAgendaOwner(id)
        return mapper.toAgendaDto(service.agregarConfiguracion(id, dto))
    }

    @DeleteMapping("/{id}/configuraciones/{configId}")
    fun eliminarConfiguracion(
        @PathVariable id: UUID,
        @PathVariable configId: UUID
    ): AgendaDto {
        authGuard.requireAgendaOwner(id)
        return mapper.toAgendaDto(service.eliminarConfiguracion(id, configId))
    }

    // ── Excepciones ───────────────────────────────────────────────────────────

    @PostMapping("/{id}/excepciones")
    fun agregarExcepcion(
        @PathVariable id: UUID,
        @RequestBody dto: ExcepcionAgendaDto
    ): AgendaDto {
        authGuard.requireAgendaOwner(id)
        return mapper.toAgendaDto(service.agregarExcepcion(id, dto))
    }

    @DeleteMapping("/{id}/excepciones/{excepcionId}")
    fun eliminarExcepcion(
        @PathVariable id: UUID,
        @PathVariable excepcionId: UUID
    ): AgendaDto {
        authGuard.requireAgendaOwner(id)
        return mapper.toAgendaDto(service.eliminarExcepcion(id, excepcionId))
    }

    // ── Slots disponibles ─────────────────────────────────────────────────────

    @GetMapping("/{id}/slots")
    fun slotsDisponibles(
        @PathVariable id: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) fecha: LocalDate
    ): List<SlotDto> = service.slotsDisponibles(id, fecha)
}
