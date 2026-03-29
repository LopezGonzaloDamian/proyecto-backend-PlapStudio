package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.dto.UsuarioActualizarDto
import ar.edu.unsam.phm.dto.UsuarioResponse
import ar.edu.unsam.phm.mapper.usuarioToDto
import ar.edu.unsam.phm.service.LibrosService
import ar.edu.unsam.phm.service.ReservaService
import ar.edu.unsam.phm.service.UsuarioService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin("*")
class PerfilUsuarioController (
    private val usuarioService: UsuarioService,
    private val librosService: LibrosService,
    private val reservaService: ReservaService
){
    @GetMapping("/perfil/{id}")
    fun datosUsuario(@PathVariable id : Int) : UsuarioResponse{
        val usuario = usuarioService.getById(id)
        val librosLeidos = reservaService.cantLibrosLeidosPorUsuario(id)
        val librosPrestados = reservaService.cantLibrosPrestadosPorUsuario(id)
        return usuarioToDto(usuario, librosLeidos, librosPrestados)
    }

    @DeleteMapping("/delete-book/{idUser}/{idBook}")
    fun deleteBookOfUser(@PathVariable idUser: Int, @PathVariable idBook : Int) {
        val usuario = usuarioService.getById(idUser)
        librosService.deleteBookOfUser(usuario, idBook)
    }

    @PutMapping("/perfil/{id}")
    fun actualizarDatosUsuario(@PathVariable id: Int, @RequestBody datos: UsuarioActualizarDto){
        usuarioService.actualizarUsuario(id, datos)
    }
}
