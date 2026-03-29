package ar.edu.unsam.phm.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

class Reserva (
    val libro: Libro,
    val usuarioLector: Usuario,
    val desde : LocalDate,
    val hasta : LocalDate
): Registrable {
    override var id: Int? = null

    init {
        require(desde <= hasta) { "La fecha de inicio no puede ser posterior a la de fin" }
    }

    fun isbn13Libro(): String = libro.isbn13

    fun duracionEnDias(): Long {
        return (ChronoUnit.DAYS.between(desde, hasta)) + 1
    }

    fun estaActiva(): Boolean {
        val hoy = LocalDate.now()
        return !hoy.isBefore(desde) && !hoy.isAfter(hasta)
    }

    fun estaProximaAVencer(): Boolean =
        estaActiva() && ChronoUnit.DAYS.between(LocalDate.now(), hasta) in 0..2

    fun fueDevuelta(): Boolean {
        return LocalDate.now().isAfter(hasta)
    }

    fun seSuperponeConRango(desde: LocalDate, hasta: LocalDate): Boolean {
        if (desde.isEqual(hasta)) return true
        return this.desde.isBefore(hasta) && this.hasta.isAfter(desde)
    }

    fun calcularBibliokarmas(): Int {
        val baseKarma = duracionEnDias().toInt() * 5
        val plusKarma = libro.plusBibliokarmas(usuarioLector.bibliokarmas)

        return baseKarma + plusKarma
    }
}