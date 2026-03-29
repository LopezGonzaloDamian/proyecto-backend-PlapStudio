package ar.edu.unsam.phm.mapper

import ar.edu.unsam.phm.domain.Resenia
import ar.edu.unsam.phm.domain.Reserva
import ar.edu.unsam.phm.dto.booking.BookingCardDto
import ar.edu.unsam.phm.dto.booking.BookingStatus
import ar.edu.unsam.phm.dto.booking.UserReviewDto
import ar.edu.unsam.phm.service.BookingType

fun toBookingCardDto(
    booking: Reserva,
    type: BookingType,
    userReview: Resenia? = null,
    canReview: Boolean = false
): BookingCardDto {
    val book = booking.libro

    val (personLabel, personName) = when (type) {
        BookingType.FOR_ME -> "Prestado por" to "${book.duenio.nombre} ${book.duenio.apellido}"
        BookingType.BY_ME -> "Prestado a" to "${booking.usuarioLector.nombre} ${booking.usuarioLector.apellido}"
    }

    val rating =
        if (book.resenias.isEmpty()) 0.0
        else book.resenias.map { it.puntaje }.average()

    val status = when {
        booking.fueDevuelta() -> BookingStatus.DEVUELTO
        booking.estaActiva() && booking.estaProximaAVencer() -> BookingStatus.PROXIMO_A_VENCER
        booking.estaActiva() -> BookingStatus.ACTIVO
        else -> throw IllegalStateException("La reserva ${booking.id} no tiene un estado valido para BookingStatus")
    }

    return BookingCardDto(
        bookingId = booking.id!!,
        title = book.titulo,
        author = book.autor,
        image = book.imagen,
        personLabel = personLabel,
        personName = personName,
        startDate = booking.desde,
        endDate = booking.hasta,
        bibliokarmas = booking.calcularBibliokarmas(),
        rating = rating,
        status = status,
        canReview = canReview,
        hasReview = userReview != null,
        userReview = userReview?.let { UserReviewDto(it.puntaje, it.comentario) }
    )
}
