package ar.edu.unsam.phm.mapper

import ar.edu.unsam.phm.domain.Libro
import ar.edu.unsam.phm.domain.LibroColeccionable
import ar.edu.unsam.phm.domain.LibroComun
import ar.edu.unsam.phm.domain.LibroDedicado
import ar.edu.unsam.phm.dto.LibroDetalleDto

fun Libro.toDetalleDto() = LibroDetalleDto(
    id                 = this.id!!,
    titulo             = this.titulo,
    imagen             = this.imagen,
    descripcion        = this.descripcion,
    genero             = this.genero,
    autor              = this.autor,
    cantidadDePaginas  = this.cantidadDePaginas,
    isbn13             = this.isbn13,
    idioma             = this.idioma.name,
    editorial          = this.editorial,
    fechaDePublicacion = this.fechaDePublicacion,
    estado             = this.estado,
    tipoDeLibro        = when (this) {
        is LibroComun         -> "Común"
        is LibroDedicado      -> "Dedicado"
        is LibroColeccionable -> "Coleccionable"
        else                  -> "Sin tipo"
    },
    ranking = this.ranking(),
    duenio  = this.duenio.toDuenioDto()
)