package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.Libro
import ar.edu.unsam.phm.domain.LibroFactory
import ar.edu.unsam.phm.domain.Usuario
import ar.edu.unsam.phm.dto.FiltrosReservaRequest
import ar.edu.unsam.phm.dto.LibroRequest
import ar.edu.unsam.phm.domain.Resenia
import ar.edu.unsam.phm.repository.RepositorioLibros
import ar.edu.unsam.phm.repository.RepositorioResenias
import ar.edu.unsam.phm.repository.ResultadoPaginado
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class LibrosService(
    @Qualifier("repoLibros") private val repositorioLibros: RepositorioLibros,
    @Qualifier("repoResenias") private val repositorioResenias: RepositorioResenias,
    private val usuarioService: UsuarioService
) {

    fun getLibroPorId(id: Int): Libro =
        repositorioLibros.getById(id)

    fun getReseniasDeLibro(id: Int, soloUltimas: Boolean): List<Resenia> {
        repositorioLibros.getById(id)
        val resenias = repositorioResenias.findByBookId(id)

        return if (soloUltimas) resenias.takeLast(2) else resenias
    }

    fun deleteBookOfUser(user: Usuario, idBook: Int) {
        val bookToDelete = repositorioLibros.getById(idBook)
        if (bookToDelete.duenio != user) {
            throw IllegalAccessError("El usuario no puede borrar ese libro")
        }
        repositorioLibros.delete(bookToDelete)
    }

    fun librosRecomendados(): List<Libro> = repositorioLibros.librosDisponibles(LocalDate.now(), LocalDate.now())

    fun librosFiltrados(filtros: FiltrosReservaRequest): List<Libro> = repositorioLibros.librosFiltrados(filtros)

    fun crear(req: LibroRequest, duenio: Usuario): Libro {
        val libro = LibroFactory.crear(req, duenio)
        repositorioLibros.create(libro)
        return libro
    }

    fun actualizar(id: Int, req: LibroRequest, duenio: Usuario): Libro {
        val libroExistente = repositorioLibros.getById(id)

        if (libroExistente.duenio != duenio) {
            throw IllegalAccessError("No podés editar un libro que no es tuyo")
            }

        val libroActualizado = LibroFactory.crear(req, duenio).also {
            it.id = id
            it.reservas.addAll(libroExistente.reservas)
        }

        val reseniasActuales = repositorioResenias.findByBookId(id)
        reseniasActuales.forEach { resenia ->
            resenia.libro = libroActualizado
            repositorioResenias.update(resenia)
            libroActualizado.agregarResenia(resenia)
        }

        repositorioLibros.update(libroActualizado)
        return libroActualizado
    }

    fun getLibrosUsuario(
        id: Int,
        pagina: Int,
        tamanio: Int,
        campo: String,
        direccion: String,
        filtro: String
    ): ResultadoPaginado<Libro> {
        val usuario = usuarioService.getById(id)
        return repositorioLibros.buscarLibrosUsuarioPaginados(usuario, filtro, campo, direccion, pagina, tamanio)
    }
}
