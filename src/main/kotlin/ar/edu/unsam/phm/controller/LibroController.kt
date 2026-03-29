package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.dto.LibroDetalleDto
import ar.edu.unsam.phm.dto.LibroRequest
import ar.edu.unsam.phm.dto.PaginaLibrosDto
import ar.edu.unsam.phm.dto.ReseniaDetalleDto
import ar.edu.unsam.phm.dto.toReseniaDetalleDto
import ar.edu.unsam.phm.mapper.toBookCardDto
import ar.edu.unsam.phm.mapper.toDetalleDto
import ar.edu.unsam.phm.service.LibrosService
import ar.edu.unsam.phm.service.UsuarioService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/libros")
@CrossOrigin("*")
class LibroController(
    private val librosService: LibrosService,
    private val usuarioService: UsuarioService
) {
    @GetMapping("/{id}")
    fun getById(@PathVariable id: Int): LibroDetalleDto =
        librosService.getLibroPorId(id).toDetalleDto()

    @PostMapping
    fun crear(
        @Valid @RequestBody req: LibroRequest,
        @RequestParam idUsuario: Int,
    ): LibroDetalleDto {
        val duenio = usuarioService.getById(idUsuario)
        return librosService.crear(req, duenio).toDetalleDto()
    }

    @PutMapping("/{id}")
    fun actualizar(
        @PathVariable id: Int,
        @Valid @RequestBody req: LibroRequest,
        @RequestParam idUsuario: Int,
    ): LibroDetalleDto {
        val duenio = usuarioService.getById(idUsuario)
        return librosService.actualizar(id, req, duenio).toDetalleDto()
    }

    @GetMapping("/{id}/resenias")
    fun getReseniasDeLibro(
        @PathVariable id: Int,
        @RequestParam(defaultValue = "false") soloUltimas: Boolean
    ): List<ReseniaDetalleDto> {
        return librosService.getReseniasDeLibro(id, soloUltimas)
            .map { it.toReseniaDetalleDto() }
    }

    @GetMapping("/usuario/{id}")
    fun getLibrosUsuario(
        @PathVariable id: Int,
        @RequestParam(defaultValue = "1") pagina: Int,
        @RequestParam(defaultValue = "3") tamanio: Int,
        @RequestParam(defaultValue = "ninguno") campo: String,
        @RequestParam(defaultValue = "ninguno") direccion: String,
        @RequestParam(defaultValue = "Todos") filtro: String
    ): PaginaLibrosDto {
        val resultado = librosService.getLibrosUsuario(id, pagina, tamanio, campo, direccion, filtro)
        val librosDto = toBookCardDto(resultado)
        return PaginaLibrosDto(
            libros = librosDto,
            totalPaginas = resultado.totalPaginas
        )
    }
}