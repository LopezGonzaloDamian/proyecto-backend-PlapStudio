package ar.edu.unsam.phm.domain

import java.time.LocalDate
import kotlin.math.ceil

abstract class Libro (
    var titulo: String,
    var imagen: String = "",
    var descripcion: String,
    var genero: Genero,
    var autor: String,
    var cantidadDePaginas: Int,
    var isbn13: String,
    var idioma: Idioma,
    var editorial: String,
    var fechaDePublicacion: LocalDate,
    var estado: Estado,
    val duenio : Usuario
): Registrable {
    override var id: Int? = null
    val fechaDeAlta: LocalDate = LocalDate.now()

    val reservas: MutableList<Reserva> = mutableListOf()
    val resenias: MutableList<Resenia> = mutableListOf()

    abstract fun plusBibliokarmas(bibliokarmasUsuario : Int): Int

    fun estaDisponible(desde: LocalDate, hasta: LocalDate): Boolean {
        return reservas.none { it.seSuperponeConRango(desde, hasta) }
    }

    fun estaDisponibleAhora() =
        estaDisponible(LocalDate.now(), LocalDate.now())

    fun agregarResenia(resenia: Resenia) {
        resenias.add(resenia)
    }

    fun findReviewByUser(userId: Int): Resenia? =
        resenias.firstOrNull { it.usuario.id == userId }

    fun cantidadDeReservas() = reservas.size

    fun ranking(): Double {
        if (resenias.isEmpty()) return 0.0
        val promedio = resenias.map { it.puntaje }.average()
        return promedio
    }

    fun agregarReserva(reserva: Reserva) {
        reservas.add(reserva)
    }
}
//------------------------------------------
class LibroComun(
    titulo: String,
    imagen: String,
    descripcion: String,
    genero: Genero,
    autor: String,
    cantidadDePaginas: Int,
    isbn13: String,
    idioma: Idioma,
    editorial: String,
    fechaDePublicacion: LocalDate,
    estado: Estado,
    duenio : Usuario) : Libro(titulo, imagen, descripcion, genero, autor, cantidadDePaginas, isbn13, idioma, editorial, fechaDePublicacion, estado, duenio) {
        override fun plusBibliokarmas(bibliokarmasUsuario : Int): Int =
            if (bibliokarmasUsuario < 1000) {
                cantidadDePaginas * 5
            } else{
                cantidadDePaginas * 2
            }

}

class LibroDedicado(
    titulo: String,
    imagen: String,
    descripcion: String,
    genero: Genero,
    autor: String,
    cantidadDePaginas: Int,
    isbn13: String,
    idioma: Idioma,
    editorial: String,
    fechaDePublicacion: LocalDate,
    estado: Estado,
    duenio : Usuario) : Libro(titulo, imagen, descripcion, genero, autor, cantidadDePaginas, isbn13, idioma, editorial, fechaDePublicacion, estado, duenio) {
        override fun plusBibliokarmas(bibliokarmasUsuario : Int): Int =
            200 + 10 * cantidadDeReservas()

}

class LibroColeccionable(
    titulo: String,
    imagen: String,
    descripcion: String,
    genero: Genero,
    autor: String,
    cantidadDePaginas: Int,
    isbn13: String,
    idioma: Idioma,
    editorial: String,
    fechaDePublicacion: LocalDate,
    estado: Estado,
    duenio : Usuario) : Libro(titulo, imagen, descripcion, genero, autor, cantidadDePaginas, isbn13, idioma, editorial, fechaDePublicacion, estado, duenio) {
        override fun plusBibliokarmas(bibliokarmasUsuario : Int): Int =
            ceil(bibliokarmasUsuario / 5.00).toInt() + cantidadDePaginas

    }
//------------------------------------------
enum class Estado {
    excelente, muy_bueno, bueno, regular, malo
}

enum class Idioma {
    espaniol, ingles, frances, portugues, italiano, chino
}

enum class Genero {
    drama, ciencia_ficcion, romance, autoayuda, disenio, literatura_clasica
}
//------------------------------------------
