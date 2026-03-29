package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Reserva
import ar.edu.unsam.phm.mapper.usuarioToDto
import ar.edu.unsam.phm.mapper.toDetalleDto
import java.time.LocalDate

data class ReservaDto (
    var libro: LibroDetalleDto,
    var usuario: UsuarioResponse,
    var desde: LocalDate,
    var hasta: LocalDate,
    val biblioKarmas: Int
)

fun reservaToDto(reserva: Reserva, biblioKarmas: Int): ReservaDto {
    return ReservaDto(
        libro = reserva.libro.toDetalleDto(),
        usuario = usuarioToDto(reserva.usuarioLector),
        desde = reserva.desde,
        hasta = reserva.hasta,
        biblioKarmas = biblioKarmas
    )
}

data class FiltrosReservaRequest(
    val titulo: String? = null,
    val genero: List<String> = emptyList(),
    val desdePaginas: Int? = null,
    val hastaPaginas: Int? = null,
    val desdeFecha: LocalDate? = null,
    val hastaFecha: LocalDate? = null,
    val isbn13: String? = null,
    val autor: String? = null,
    val usernamePrestador: String? = null
)

data class ConfirmarReservaRequest(
    val idUsuario: Int,
    val desde: LocalDate,
    val hasta: LocalDate
)