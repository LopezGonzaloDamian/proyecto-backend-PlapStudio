package ar.edu.unsam.phm.mapper

import ar.edu.unsam.phm.domain.Libro
import ar.edu.unsam.phm.dto.LibroTarjetaDto
import ar.edu.unsam.phm.repository.ResultadoPaginado
import java.time.LocalDate

fun toBookCardDto(resultado : ResultadoPaginado<Libro>) : List<LibroTarjetaDto>{
    return resultado.elementos.map { libro ->
        LibroTarjetaDto(
            id = libro.id!!,
            titulo = libro.titulo,
            imagen = libro.imagen,
            genero = libro.genero,
            autor = libro.autor,
            fechaDeAlta = libro.fechaDeAlta,
            disponible = libro.estaDisponible(LocalDate.now(), LocalDate.now())
        )
    }
}