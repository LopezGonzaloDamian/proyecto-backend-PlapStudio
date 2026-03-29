package plapstudio.agendify.domain

import java.time.LocalDate

enum class Genero {
    OTRO
}

data class Libro(
    var titulo: String = "",
    var isbn13: String = "",
    var autor: String = "",
    var duenio: Usuario = Usuario(),
    var genero: Genero = Genero.OTRO,
    var cantidadDePaginas: Int = 0,
    var fechaDePublicacion: LocalDate = LocalDate.now()
) : Registrable() {
    fun estaDisponible(desde: LocalDate, hasta: LocalDate): Boolean = desde <= hasta

    fun estaDisponibleAhora(): Boolean = true
}
