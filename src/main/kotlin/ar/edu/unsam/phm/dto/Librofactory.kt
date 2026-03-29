package ar.edu.unsam.phm.domain

import ar.edu.unsam.phm.dto.LibroRequest
import java.time.LocalDate

object LibroFactory {

    fun crear(req: LibroRequest, duenio: Usuario): Libro {
        val titulo             = req.titulo
        val imagen             = req.imagen
        val descripcion        = req.descripcion
        val genero             = Genero.valueOf(req.genero)
        val autor              = req.autor
        val cantidadDePaginas  = req.cantidadDePaginas
        val isbn13             = req.isbn13
        val idioma             = Idioma.valueOf(req.idioma)
        val editorial          = req.editorial
        val fechaDePublicacion = LocalDate.parse(req.fechaDePublicacion)
        val estado             = Estado.valueOf(req.estado)

        return when (req.tipo) {
            "dedicado"      -> LibroDedicado(titulo, imagen, descripcion, genero, autor, cantidadDePaginas, isbn13, idioma, editorial, fechaDePublicacion, estado, duenio)
            "coleccionable" -> LibroColeccionable(titulo, imagen, descripcion, genero, autor, cantidadDePaginas, isbn13, idioma, editorial, fechaDePublicacion, estado, duenio)
            "comun"         -> LibroComun(titulo, imagen, descripcion, genero, autor, cantidadDePaginas, isbn13, idioma, editorial, fechaDePublicacion, estado, duenio)
            else            -> throw IllegalArgumentException("Tipo de libro desconocido: ${req.tipo}")
        }
    }
}