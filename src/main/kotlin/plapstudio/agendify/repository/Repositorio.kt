package plapstudio.agendify.repository

import plapstudio.agendify.domain.*
import plapstudio.agendify.errors.NotFoundException
import kotlin.collections.filter
import java.time.LocalDate
import kotlin.math.ceil
import plapstudio.agendify.dto.FiltrosReservaRequest

open class Repositorio<T: Registrable>(
    val condicionSearch: SearchStrategy<T>

) {

    var contadorId = 1
    val elementos: MutableSet<T> = mutableSetOf<T>()
    var nombreTipo: String = "elemento"

    fun create(elemento: T) {
        if (elementos.isEmpty()) nombreTipo = elemento.let { it::class.simpleName } ?: "elemento"
        elemento.id = contadorId++
        elementos.add(elemento)
    }

    open fun delete(elemento: T) {
        if (elementos.contains(elemento)) {
            elementos.remove(elemento)
        } else {
            throw NotFoundException("No se ha encontrado el $nombreTipo seleccionado")
        }

    }

    fun update(elementoNuevo: T) {
        val elementoViejo = elementos.find { it.id == elementoNuevo.id }

        if (elementoViejo == null) {
            throw NotFoundException("No se ha encontrado el $nombreTipo seleccionado")
        } else {
            delete(elementoViejo)

            elementos.add(elementoNuevo)
        }
    }

    fun getById(id: Int?): T {
        val elementoBuscado = elementos.find { it.id == id }

        if (elementoBuscado != null) {
            return elementoBuscado
        } else {
            throw NotFoundException("No se ha encontrado el $nombreTipo seleccionado")
        }

    }

    fun search(valor: String): List<T> {
        val resultados = condicionSearch.search(elementos, valor)

        if (resultados.isEmpty()) {
            throw NotFoundException("No se encontraron $nombreTipo para el valor: $valor")
        }
        return resultados
    }

    // Creados pa testeo
    fun clear() {
        elementos.clear()
        contadorId = 1
    }

    fun getAll(): List<T> {
        val lista = elementos.toList()

        if (lista.isEmpty()) {
            throw NotFoundException("No hay elementos disponibles para mostrar")
        }

        return lista
    }
}

//--------------------------------------------------------------

class RepositorioUsuarios(
    condicionSearch: SearchStrategy<Usuario>
) : Repositorio<Usuario>(condicionSearch) {
    fun existeUsername(username: String) =
        elementos.any { it.username.equals(username, ignoreCase = true) }

    fun buscarPorEmail(email: String): Usuario? =
        elementos.firstOrNull { it.email.equals(email, ignoreCase = true) }
}

class RepositorioLibros(
    condicionSearch: SearchStrategy<Libro>
) : Repositorio<Libro>(condicionSearch) {
    fun librosDeUsuario(usuario: Usuario) : List<Libro> =
        elementos.filter { it.duenio == usuario }

    fun librosDisponibles(desde: LocalDate, hasta: LocalDate): List<Libro> =
        elementos.filter { it.estaDisponible(desde, hasta) }

    fun librosFiltrados(filtros: FiltrosReservaRequest): List<Libro> =
        elementos
        .asSequence()
        .filter { libro ->
            filtros.titulo.isNullOrBlank() ||
                libro.titulo.contains(filtros.titulo, ignoreCase = true)
        }
        .filter { libro ->
            filtros.isbn13.isNullOrBlank() ||
                libro.isbn13.equals(filtros.isbn13, ignoreCase = true)
        }
        .filter { libro ->
            filtros.autor.isNullOrBlank() ||
                libro.autor.contains(filtros.autor, ignoreCase = true)
        }
        .filter { libro ->
            filtros.usernamePrestador.isNullOrBlank() ||
                libro.duenio.username.equals(filtros.usernamePrestador, ignoreCase = true)
        }
        .filter { libro ->
            filtros.genero.isEmpty() ||
                filtros.genero.any { genero ->
                    libro.genero.name.equals(genero.trim(), ignoreCase = true)
                }
        }
        .filter { libro ->
            filtros.desdePaginas == null ||
                filtros.hastaPaginas == null ||
                libro.cantidadDePaginas in filtros.desdePaginas..filtros.hastaPaginas
        }
        .filter { libro ->
            filtros.desdeFecha == null ||
                filtros.hastaFecha == null ||
                libro.estaDisponible(filtros.desdeFecha, filtros.hastaFecha)
        }
        .toList()

    fun buscarLibrosUsuarioPaginados(
        usuario: Usuario,
        filtro: String,
        campo: String,
        direccion: String,
        pagina: Int,
        tamanio: Int
    ): ResultadoPaginado<Libro> {
        var resultado = elementos.filter { it.duenio == usuario }

        when (filtro.lowercase()) {
            "disponible" -> resultado = resultado.filter { it.estaDisponibleAhora() }
            "prestado" -> resultado = resultado.filter { !it.estaDisponibleAhora()}
        }

        if (campo.lowercase() != "ninguno") {
            val comparador = when (campo.lowercase()) {
                "estado" -> compareBy<Libro> { it.estaDisponibleAhora() }
                "fechaagregado" -> compareBy { it.fechaDePublicacion }
                else -> compareBy { it.id }
            }

            resultado = if (direccion.lowercase() == "desc") {
                resultado.sortedWith(comparador.reversed())
            } else {
                resultado.sortedWith(comparador)
            }
        }

        val totalElementos = resultado.size
        val totalPaginas = if (totalElementos == 0) 1 else ceil(totalElementos.toDouble() / tamanio).toInt()

        val indiceInicial = if (pagina > 0) (pagina - 1) * tamanio else 0
        val indiceFinal = minOf(indiceInicial + tamanio, totalElementos)

        val elementosPaginados = if (indiceInicial >= totalElementos) emptyList()
        else resultado.subList(indiceInicial, indiceFinal)

        return ResultadoPaginado(elementosPaginados, totalPaginas)
    }
}

class RepositorioReservas(
    condicionSearch: SearchStrategy<Reserva>
) : Repositorio<Reserva>(condicionSearch) {
    fun cantLibrosLeidosPorUsuario(userId:Int) : Int =
        elementos.filter { it.usuarioLector.id == userId }.size

    fun cantLibrosPrestadosPorUsuario(userId: Int) : Int =
        elementos.filter { it.libro.duenio.id == userId }.size

    fun findBookingsForMe(userId: Int, search: String?): List<Reserva> =
        findBookings(search) { it.usuarioLector.id == userId }

    fun findBookingsByMe(userId: Int, search: String?): List<Reserva> =
        findBookings(search) { it.libro.duenio.id == userId }

    fun findLatestReturnedBookingForUserAndBook(userId: Int, bookId: Int): Reserva? =
        elementos
            .filter { it.usuarioLector.id == userId && it.libro.id == bookId }
            .filter { it.fueDevuelta() }
            .maxByOrNull { it.hasta }

    private fun findBookings(
        search: String?,
        userFilter: (Reserva) -> Boolean
    ): List<Reserva> {
        return elementos.filter { reserva ->
            userFilter(reserva) &&
                (
                    search.isNullOrBlank() ||
                    reserva.libro.titulo.contains(search, ignoreCase = true) ||
                    reserva.libro.autor.contains(search, ignoreCase = true)
                )
        }
    }
}

class RepositorioResenias(
    condicionSearch: SearchStrategy<Resenia>
) : Repositorio<Resenia>(condicionSearch) {
    fun findByBookId(bookId: Int): List<Resenia> =
        elementos.filter { it.libro.id == bookId }

    fun findByUserAndBook(userId: Int, bookId: Int): Resenia? =
        elementos.firstOrNull { it.usuario.id == userId && it.libro.id == bookId }

    fun averageRatingForBook(bookId: Int): Double {
        val reviews = findByBookId(bookId)
        return if (reviews.isEmpty()) 0.0 else reviews.map { it.puntaje }.average()
    }
}

//--------------------------------------------------------------


class RepositorioException(msj: String) : Throwable()

class SearchException(msj: String) : Throwable()


//--------------------------------------------------------------

abstract class SearchStrategy<T : Registrable> {
    fun search(coleccion: Set<T>, valor: String): List<T> {
        return coleccion.filter{criterio(it, valor)}
    }

    abstract fun criterio(objeto : T, valor: String):Boolean

    fun coincideNombre(libro: Libro, valor: String) =
        libro.titulo.contains(valor, ignoreCase = true) ||
                libro.isbn13.equals(valor, ignoreCase = true)
}

class SearchUsuario : SearchStrategy<Usuario>() {
    override fun criterio(objeto: Usuario, valor: String) =
        objeto.nombre.contains(valor, ignoreCase = true) ||
                objeto.apellido.contains(valor, ignoreCase = true) ||
                objeto.username.equals(valor, ignoreCase = true)
}

class SearchLibro : SearchStrategy<Libro>() {
    override fun criterio(objeto: Libro, valor: String) =
        coincideNombre(objeto, valor)
}

class SearchReserva : SearchStrategy<Reserva>() {
    override fun criterio(objeto: Reserva, valor: String) =
        objeto.isbn13Libro().equals(valor, ignoreCase = true) && objeto.estaActiva()
}

class SearchResenia : SearchStrategy<Resenia>() {
    override fun criterio(objeto: Resenia, valor: String) =
        objeto.libro.titulo.contains(valor, ignoreCase = true) ||
            objeto.usuario.username.equals(valor, ignoreCase = true)
}

data class ResultadoPaginado<T>(
    val elementos: List<T>,
    val totalPaginas: Int
)
