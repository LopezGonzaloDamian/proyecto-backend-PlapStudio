package plapstudio.agendify.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import plapstudio.agendify.dto.FavoritoDto
import plapstudio.agendify.dto.FavoritoToggleRequest
import plapstudio.agendify.dto.Mapper
import plapstudio.agendify.service.FavoritoService

@RestController
@RequestMapping("/favoritos")
@CrossOrigin("*")
class FavoritoController(
    private val service: FavoritoService,
    private val mapper:  Mapper
) {

    @GetMapping("/cliente/{clienteId}")
    fun listar(@PathVariable clienteId: Long): List<FavoritoDto> =
        service.findByCliente(clienteId).map { mapper.toFavoritoDto(it) }

    @PostMapping("/toggle")
    fun toggle(@RequestBody req: FavoritoToggleRequest): ResponseEntity<FavoritoDto> {
        val favorito = service.toggle(req.clienteId, req.profesionalId)
        return if (favorito != null) ResponseEntity.ok(mapper.toFavoritoDto(favorito))
        else                         ResponseEntity.noContent().build()
    }
}
