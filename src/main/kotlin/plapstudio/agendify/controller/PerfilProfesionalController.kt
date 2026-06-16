package plapstudio.agendify.controller

import org.springframework.web.bind.annotation.*
import plapstudio.agendify.auth.AuthGuard
import plapstudio.agendify.dto.Mapper
import plapstudio.agendify.dto.ProfesionalDto
import plapstudio.agendify.dto.ProfesionalSummaryDto
import plapstudio.agendify.dto.ProfesionalUpdateRequest
import plapstudio.agendify.service.PerfilProfesionalService
import java.time.LocalDate

@RestController
@RequestMapping("/profesionales")
@CrossOrigin("*")
class PerfilProfesionalController(
    private val service: PerfilProfesionalService,
    private val mapper:  Mapper,
    private val authGuard: AuthGuard
) {

    @GetMapping
    fun buscar(
        @RequestParam(required = false) query: String?,
        @RequestParam(required = false) especialidad: String?,
        @RequestParam(required = false) localidad: String?,
        @RequestParam(required = false) fechaDeseada: LocalDate?
    ): List<ProfesionalSummaryDto> =
        service.buscar(query, especialidad, localidad, fechaDeseada).map { mapper.toProfesionalSummaryDto(it) }

    @GetMapping("/destacados")
    fun destacados(): List<ProfesionalSummaryDto> =
        service.findDestacados().map { mapper.toProfesionalSummaryDto(it) }

    @GetMapping("/{id}")
    fun detalle(@PathVariable id: Long): ProfesionalDto {
        val perfil  = service.findById(id)
        val agendas = service.agendasDe(id)
        return mapper.toProfesionalDto(perfil, agendas)
    }

    @PutMapping("/{id}")
    fun actualizar(@PathVariable id: Long, @RequestBody req: ProfesionalUpdateRequest): ProfesionalDto {
        authGuard.requireProfesional(id)
        val perfil  = service.update(id, req)
        val agendas = service.agendasDe(id)
        return mapper.toProfesionalDto(perfil, agendas)
    }
}
