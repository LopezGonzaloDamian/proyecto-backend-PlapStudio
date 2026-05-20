package plapstudio.agendify.controller

import org.springframework.web.bind.annotation.*
import plapstudio.agendify.auth.AuthGuard
import plapstudio.agendify.dto.ClienteDto
import plapstudio.agendify.dto.Mapper
import plapstudio.agendify.service.PerfilClienteService

@RestController
@RequestMapping("/clientes")
@CrossOrigin("*")
class PerfilClienteController(
    private val service: PerfilClienteService,
    private val mapper:  Mapper,
    private val authGuard: AuthGuard
) {

    @GetMapping
    fun listar(): List<ClienteDto> {
        authGuard.requireAdmin()
        return service.findAll().map { mapper.toClienteDto(it) }
    }

    @GetMapping("/buscar")
    fun buscarPorEmail(@RequestParam email: String): ClienteDto {
        authGuard.requireAuthenticated()
        return mapper.toClienteDto(service.findByEmail(email))
    }

    @GetMapping("/{id}")
    fun detalle(@PathVariable id: Long): ClienteDto {
        authGuard.requireCliente(id)
        return mapper.toClienteDto(service.findById(id))
    }

    @GetMapping("/profesional/{profesionalId}")
    fun clientesDeProfesional(@PathVariable profesionalId: Long): List<ClienteDto> {
        authGuard.requireProfesionalOrAssignedAssistant(profesionalId)
        return service.clientesDeProfesional(profesionalId).map { mapper.toClienteDto(it) }
    }
}
