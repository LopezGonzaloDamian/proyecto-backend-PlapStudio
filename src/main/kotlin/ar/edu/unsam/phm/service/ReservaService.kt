package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.Libro
import ar.edu.unsam.phm.domain.Resenia
import ar.edu.unsam.phm.domain.Reserva
import ar.edu.unsam.phm.domain.Usuario
import ar.edu.unsam.phm.dto.FiltrosReservaRequest
import ar.edu.unsam.phm.dto.booking.BookingPageDto
import ar.edu.unsam.phm.errors.BusinessException
import ar.edu.unsam.phm.mapper.toBookingCardDto
import ar.edu.unsam.phm.repository.RepositorioReservas
import ar.edu.unsam.phm.repository.RepositorioResenias
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.LocalDate
import kotlin.math.ceil

@Component
class ReservaService(
    @Qualifier("repoReservas") private val repositorioReservas: RepositorioReservas,
    @Qualifier("repoResenias") private val repositorioResenias: RepositorioResenias,
    private val librosService: LibrosService,
    private val usuarioService: UsuarioService
) {

    fun getMyBookings(
        userId: Int,
        type: BookingType,
        search: String?,
        page: Int,
        size: Int
    ): BookingPageDto {
        if (page < 0) throw BusinessException("La pagina no puede ser negativa")
        if (size <= 0) throw BusinessException("El size debe ser mayor a 0")

        usuarioService.getById(userId)

        val normalizedSearch = search?.trim()?.takeIf { it.isNotBlank() }

        val filtered = when (type) {
            BookingType.FOR_ME -> repositorioReservas.findBookingsForMe(userId, normalizedSearch)
            BookingType.BY_ME -> repositorioReservas.findBookingsByMe(userId, normalizedSearch)
        }
            .filter { it.estaActiva() || it.fueDevuelta() }
            .sortedWith(compareByDescending<Reserva> { it.desde }.thenBy { it.id })

        val totalItems = filtered.size
        val totalPages = if (totalItems == 0) 0 else ceil(totalItems.toDouble() / size).toInt()

        val fromIndex = page * size
        val items =
            if (fromIndex >= totalItems) {
                emptyList()
            } else {
                val toIndex = minOf(fromIndex + size, totalItems)
                filtered.subList(fromIndex, toIndex)
            }

        return BookingPageDto(
            items = items.map { booking ->
                val userReview =
                    if (type == BookingType.FOR_ME) {
                        repositorioResenias.findByUserAndBook(
                            booking.usuarioLector.id!!,
                            booking.libro.id!!
                        )
                    } else {
                        null
                    }

                val latestReturnedBooking =
                    if (type == BookingType.FOR_ME) {
                        repositorioReservas.findLatestReturnedBookingForUserAndBook(
                            booking.usuarioLector.id!!,
                            booking.libro.id!!
                        )
                    } else {
                        null
                    }

                val canReview = latestReturnedBooking?.id == booking.id

                toBookingCardDto(booking, type, userReview, canReview)
            },
            page = page,
            size = size,
            totalItems = totalItems,
            totalPages = totalPages,
            hasNext = page + 1 < totalPages,
            hasPrevious = page > 0
        )
    }

    fun generarReservaPosible(
        libro: Libro,
        usuario: Usuario,
        desde: LocalDate? = null,
        hasta: LocalDate? = null
    ): Reserva {
        val fechaDesde = desde ?: LocalDate.now()
        val fechaHasta = hasta ?: fechaDesde

        return Reserva(libro, usuario, fechaDesde, fechaHasta)
    }

    fun listaReservasPosibles(
        libros: List<Libro>,
        usuario: Usuario,
        filtros: FiltrosReservaRequest
    ): List<Reserva> =
        libros.map { libro ->
            generarReservaPosible(
                libro,
                usuario,
                filtros.desdeFecha,
                filtros.hastaFecha
            )
        }

    fun confirmarReserva(idLibro: Int, idUsuario: Int, desde: LocalDate, hasta: LocalDate) {
        val libro = librosService.getLibroPorId(idLibro)
        val usuario = usuarioService.getById(idUsuario)

        if (!libro.estaDisponible(desde, hasta)) {
            throw BusinessException("El libro no está disponible para las fechas seleccionadas.")
        }

        val reserva = usuario.reservar(libro, desde, hasta)
        repositorioReservas.create(reserva)
    }

    fun reseniarReserva(bookingId: Int, puntaje: Int, comentario: String) {
        if (puntaje !in 1..5) {
            throw BusinessException("El puntaje debe estar entre 1 y 5")
        }

        if (comentario.isBlank()) {
            throw BusinessException("El comentario no puede estar vacio")
        }

        val reserva = repositorioReservas.getById(bookingId)

        if (!reserva.fueDevuelta()) {
            throw BusinessException("No podes reseñar una reserva que no fue devuelta")
        }

        val usuario = reserva.usuarioLector
        val libro = reserva.libro
        val libroActual = librosService.getLibroPorId(libro.id!!)
        val latestReturnedBooking =
            repositorioReservas.findLatestReturnedBookingForUserAndBook(usuario.id!!, libro.id!!)

        if (latestReturnedBooking?.id != reserva.id) {
            throw BusinessException("Solo podes reseñar o editar la reseña desde tu ultima reserva devuelta de este libro")
        }

        val reseniaExistente = repositorioResenias.findByUserAndBook(usuario.id!!, libro.id!!)

        if (reseniaExistente == null) {
            val nuevaResenia = Resenia(
                usuario = usuario,
                libro = libroActual,
                puntaje = puntaje,
                comentario = comentario
            )
            repositorioResenias.create(nuevaResenia)
            libro.agregarResenia(nuevaResenia)
            if (libroActual !== libro) {
                libroActual.agregarResenia(nuevaResenia)
            }
        } else {
            reseniaExistente.libro = libroActual
            reseniaExistente.actualizar(puntaje, comentario)
            repositorioResenias.update(reseniaExistente)
            if (libroActual.resenias.none { it.id == reseniaExistente.id }) {
                libroActual.agregarResenia(reseniaExistente)
            }
        }
    }

    fun cantLibrosLeidosPorUsuario(userId: Int) =
        repositorioReservas.cantLibrosLeidosPorUsuario(userId)

    fun cantLibrosPrestadosPorUsuario(userId: Int) =
        repositorioReservas.cantLibrosPrestadosPorUsuario(userId)
}
