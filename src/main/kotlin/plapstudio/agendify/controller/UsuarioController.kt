package plapstudio.agendify.controller

import plapstudio.agendify.domain.Usuario
import plapstudio.agendify.service.UsuarioService
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/usuarios")
@CrossOrigin("*")
class UsuarioController(private val usuarioService: UsuarioService) {

    @GetMapping
    fun getAll(): List<Usuario> = usuarioService.findAll()

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): Usuario = usuarioService.findById(id)

    @PostMapping
    fun create(@RequestBody usuario: Usuario): Usuario = usuarioService.create(usuario)

    @PutMapping("/{id}")
    fun update(@PathVariable id: Long, @RequestBody datos: Usuario): Usuario =
        usuarioService.update(id, datos)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: Long) = usuarioService.delete(id)
}
