package plapstudio.agendify.dto

import java.time.LocalDate

data class FiltrosReservaRequest(
    val titulo: String? = null,
    val isbn13: String? = null,
    val autor: String? = null,
    val usernamePrestador: String? = null,
    val genero: List<String> = emptyList(),
    val desdePaginas: Int? = null,
    val hastaPaginas: Int? = null,
    val desdeFecha: LocalDate? = null,
    val hastaFecha: LocalDate? = null
)
