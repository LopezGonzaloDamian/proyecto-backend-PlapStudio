package plapstudio.agendify.controller

import org.springframework.web.bind.annotation.*
import plapstudio.agendify.dto.Mapper
import plapstudio.agendify.dto.UsuarioDto
import plapstudio.agendify.service.UsuarioService

@RestController
@RequestMapping("/usuarios")
@CrossOrigin("*")
class UsuarioController(
    private val usuarioService: UsuarioService,
    private val mapper:         Mapper
) {

    @GetMapping
    fun getAll(@RequestParam(required = false) rol: String?): List<UsuarioDto> =
        (rol?.let { usuarioService.findByRol(it) } ?: usuarioService.findAll()).map { mapper.toUsuarioDto(it) }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): UsuarioDto =
        mapper.toUsuarioDto(usuarioService.findById(id))

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = usuarioService.delete(id)
}
