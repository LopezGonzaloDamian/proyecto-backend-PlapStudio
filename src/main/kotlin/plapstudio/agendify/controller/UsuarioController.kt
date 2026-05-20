package plapstudio.agendify.controller

import org.springframework.web.bind.annotation.*
import plapstudio.agendify.auth.AuthGuard
import plapstudio.agendify.dto.Mapper
import plapstudio.agendify.dto.UsuarioDto
import plapstudio.agendify.service.UsuarioService

@RestController
@RequestMapping("/usuarios")
@CrossOrigin("*")
class UsuarioController(
    private val usuarioService: UsuarioService,
    private val mapper:         Mapper,
    private val authGuard: AuthGuard
) {

    @GetMapping
    fun getAll(@RequestParam(required = false) rol: String?): List<UsuarioDto> {
        val usuario = authGuard.requireAuthenticated()
        if (!usuario.esAdmin()) {
            val rolNormalizado = rol?.uppercase()
            if (!(usuario.esProfesional() && rolNormalizado == "ASISTENTE")) {
                throw plapstudio.agendify.errors.ForbiddenException("No tienes permiso para consultar esta lista")
            }
        }
        return (rol?.let { usuarioService.findByRol(it) } ?: usuarioService.findAll()).map { mapper.toUsuarioDto(it) }
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): UsuarioDto {
        authGuard.requireUser(id)
        return mapper.toUsuarioDto(usuarioService.findById(id))
    }

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) {
        authGuard.requireAdmin()
        usuarioService.delete(id)
    }
}
