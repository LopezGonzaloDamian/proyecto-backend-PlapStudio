package plapstudio.agendify.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import plapstudio.agendify.auth.AuthGuard
import plapstudio.agendify.dto.FavoritoDto
import plapstudio.agendify.dto.FavoritoToggleRequest
import plapstudio.agendify.dto.Mapper
import plapstudio.agendify.service.FavoritoService

@RestController
@RequestMapping("/favoritos")
@CrossOrigin("*")
class FavoritoController(
    private val service: FavoritoService,
    private val mapper:  Mapper,
    private val authGuard: AuthGuard
) {

    @GetMapping("/cliente/{clienteId}")
    fun listar(@PathVariable clienteId: Long): List<FavoritoDto> {
        authGuard.requireCliente(clienteId)
        return service.findByCliente(clienteId).map { mapper.toFavoritoDto(it) }
    }

    @PostMapping("/toggle")
    fun toggle(@Valid @RequestBody req: FavoritoToggleRequest): ResponseEntity<FavoritoDto> {
        authGuard.requireCliente(req.clienteId)
        val favorito = service.toggle(req.clienteId, req.profesionalId)
        return if (favorito != null) ResponseEntity.ok(mapper.toFavoritoDto(favorito))
        else                         ResponseEntity.noContent().build()
    }
}
