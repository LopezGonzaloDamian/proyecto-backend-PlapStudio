package plapstudio.agendify.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import plapstudio.agendify.auth.AuthGuard
import plapstudio.agendify.dto.Mapper
import plapstudio.agendify.dto.ResenaCreateRequest
import plapstudio.agendify.dto.ResenaDto
import plapstudio.agendify.errors.ForbiddenException
import plapstudio.agendify.service.ResenaProfesionalService

@RestController
@RequestMapping("/resenas")
@CrossOrigin("*")
class ResenaProfesionalController(
    private val service: ResenaProfesionalService,
    private val mapper: Mapper,
    private val authGuard: AuthGuard
) {

    @GetMapping("/profesional/{profesionalId}")
    fun getByProfesional(@PathVariable profesionalId: Long): List<ResenaDto> =
        service.findByProfesional(profesionalId).map { mapper.toResenaDto(it) }

    @PostMapping
    fun crear(@Valid @RequestBody req: ResenaCreateRequest): ResenaDto {
        val usuario = authGuard.requireAuthenticated()
        val clienteId = usuario.perfilCliente?.id
            ?: throw ForbiddenException("Solo clientes pueden dejar resenas")
        return mapper.toResenaDto(service.crear(clienteId, req))
    }
}
