package plapstudio.agendify.domain

import java.time.LocalDate

class Usuario(
    var nombre: String,
    var apellido: String,
    val email: String,
    val username: String,
    val password: String,
    var avatar: String,
    var celular: String,
    var ciudad: String,
    var descripcion: String,
    var bibliokarmas: Int = 0,
    val fechaDeAlta: LocalDate,
    var tipoUsuario: TipoDeUsuario
) : Registrable {
    override var id: Int? = null

    fun reservar(libro: Libro, desde: LocalDate, hasta: LocalDate): Reserva {
        if (!puedeReservar()) {
            throw Exception("No podÃƒÂ©s reservar")
        }

        if (!libro.estaDisponible(desde, hasta)) {
            throw Exception("El libro no estÃƒÂ¡ disponible")
        }

        val reserva = Reserva(libro, this, desde, hasta)
        val bibliokarmasGanados = reserva.calcularBibliokarmas()

        sumarBibliokarmas(bibliokarmasGanados)
        
        libro.agregarReserva(reserva)
 
        return reserva
    }

    fun puedeReservar(): Boolean {
        return tipoUsuario == TipoDeUsuario.lector ||
            tipoUsuario == TipoDeUsuario.lector_publicador
    }

    fun puedePublicar(): Boolean {
        return tipoUsuario == TipoDeUsuario.publicador ||
            tipoUsuario == TipoDeUsuario.lector_publicador
    }

    fun sumarBibliokarmas(valor: Int) {
        bibliokarmas += valor
    }
}

// ------------------------------

enum class TipoDeUsuario {
    lector, publicador, lector_publicador
}