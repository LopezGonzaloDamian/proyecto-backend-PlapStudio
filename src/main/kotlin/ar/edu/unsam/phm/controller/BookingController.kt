package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.dto.ConfirmarReservaRequest
import ar.edu.unsam.phm.dto.FiltrosReservaRequest
import ar.edu.unsam.phm.dto.ReservaDto
import ar.edu.unsam.phm.dto.booking.BookingPageDto
import ar.edu.unsam.phm.dto.reservaToDto
import ar.edu.unsam.phm.service.BookingType
import ar.edu.unsam.phm.service.LibrosService
import ar.edu.unsam.phm.service.ReservaService
import ar.edu.unsam.phm.service.UsuarioService
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin("*") 
class BookingController(
    private val usuarioService: UsuarioService,
    private val librosService: LibrosService,
    private val reservaService: ReservaService
) {

    @GetMapping("/users/{userId}/my-bookings")
    fun getMyBookings(
        @PathVariable userId: Int,
        @RequestParam type: BookingType,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "4") size: Int
    ): BookingPageDto {
        return reservaService.getMyBookings(userId, type, search, page, size)
    }

    @GetMapping("/reservas-recomendadas/{id}")
    fun getReservasRecomendadas(@PathVariable id: Int): List<ReservaDto> {
        val usuario = usuarioService.getById(id)
        val librosRecomendados = librosService.librosRecomendados()

        return librosRecomendados.map { libro ->
            val reserva = reservaService.generarReservaPosible(libro, usuario)
            reservaToDto(reserva, reserva.calcularBibliokarmas())
        }
    }

    @PostMapping("/reservas-filtradas/{id}")
    fun getReservasFiltradas(
        @PathVariable id: Int,
        @RequestBody filtros: FiltrosReservaRequest
    ): List<ReservaDto> {
        val usuario = usuarioService.getById(id)
        val librosFiltrados = librosService.librosFiltrados(filtros)
        val reservasPosibles = reservaService.listaReservasPosibles(librosFiltrados, usuario, filtros)

        return reservasPosibles.map { reserva ->
            reservaToDto(reserva, reserva.calcularBibliokarmas())
        }
    }

    @PostMapping("/libros/{idLibro}/confirmar-reserva")
    fun confirmarReserva(
        @PathVariable idLibro: Int,
        @RequestBody body: ConfirmarReservaRequest
    ) {
        reservaService.confirmarReserva(idLibro, body.idUsuario, body.desde, body.hasta)
    }
}