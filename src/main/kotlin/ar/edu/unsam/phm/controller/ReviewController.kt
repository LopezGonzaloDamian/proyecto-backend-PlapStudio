package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.dto.booking.ReviewBookingRequestDto
import ar.edu.unsam.phm.service.ReservaService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/bookings")
@CrossOrigin("*")
class ReviewController(
    private val reservaService: ReservaService
) {

    @PostMapping("/{bookingId}/review")
    fun reviewBooking(
        @PathVariable bookingId: Int,
        @RequestBody request: ReviewBookingRequestDto
    ) {
        reservaService.reseniarReserva(bookingId, request.puntaje, request.comentario)
    }
}
