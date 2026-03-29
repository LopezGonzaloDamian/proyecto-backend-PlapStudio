package plapstudio.agendify.domain

import java.time.LocalDate

data class Reserva(
    var usuarioLector: Usuario = Usuario(),
    var libro: Libro = Libro(),
    var hasta: LocalDate = LocalDate.now()
) : Registrable() {
    fun fueDevuelta(): Boolean = hasta.isBefore(LocalDate.now())

    fun isbn13Libro(): String = libro.isbn13

    fun estaActiva(): Boolean = !fueDevuelta()
}
