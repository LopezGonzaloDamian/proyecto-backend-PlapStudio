package plapstudio.agendify.controller

import org.springframework.web.bind.annotation.*
import plapstudio.agendify.dto.ClienteDto
import plapstudio.agendify.dto.Mapper
import plapstudio.agendify.service.PerfilClienteService

@RestController
@RequestMapping("/clientes")
@CrossOrigin("*")
class PerfilClienteController(
    private val service: PerfilClienteService,
    private val mapper:  Mapper
) {

    @GetMapping
    fun listar(): List<ClienteDto> = service.findAll().map { mapper.toClienteDto(it) }

    @GetMapping("/{id}")
    fun detalle(@PathVariable id: Long): ClienteDto = mapper.toClienteDto(service.findById(id))

    @GetMapping("/profesional/{profesionalId}")
    fun clientesDeProfesional(@PathVariable profesionalId: Long): List<ClienteDto> =
        service.clientesDeProfesional(profesionalId).map { mapper.toClienteDto(it) }
}
