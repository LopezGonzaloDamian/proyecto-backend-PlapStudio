package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Estado
import ar.edu.unsam.phm.domain.Genero
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class LibroTarjetaDto(
    val id: Int,
    var titulo: String,
    var imagen: String = "",
    var genero: Genero,
    var autor: String,
    var fechaDeAlta: LocalDate,
    var disponible: Boolean,
)

data class LibroDetalleDto(
    val id: Int,
    val titulo: String,
    val imagen: String,
    val descripcion: String,
    val genero: Genero,
    val autor: String,
    val cantidadDePaginas: Int,
    val isbn13: String,
    val idioma: String,
    val editorial: String,
    val fechaDePublicacion: LocalDate,
    val estado: Estado,
    val tipoDeLibro: String,
    val ranking: Double,
    val duenio: DuenioDto,
)

data class LibroRequest(

    @field:NotBlank(message = "El título no puede estar vacío")
    @field:Size(max = 200, message = "El título no puede superar los 200 caracteres")
    val titulo: String,

    @field:NotBlank(message = "La descripción no puede estar vacía")
    @field:Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    val descripcion: String,

    @field:NotBlank(message = "El género no puede estar vacío")
    val genero: String,

    @field:NotBlank(message = "El autor no puede estar vacío")
    @field:Size(max = 100, message = "El autor no puede superar los 100 caracteres")
    val autor: String,

    @field:Min(value = 1, message = "La cantidad de páginas debe ser al menos 1")
    @field:Max(value = 5000, message = "La cantidad de páginas no puede superar 5000")
    val cantidadDePaginas: Int,

    @field:NotBlank(message = "El ISBN no puede estar vacío")
    @field:Pattern(
        regexp = "^97[89]-\\d{10}$",
        message = "El ISBN debe tener el formato 978-XXXXXXXXXX o 979-XXXXXXXXXX"
    )
    val isbn13: String,

    @field:NotBlank(message = "El idioma no puede estar vacío")
    val idioma: String,

    @field:NotBlank(message = "La editorial no puede estar vacía")
    @field:Size(max = 100, message = "La editorial no puede superar los 100 caracteres")
    val editorial: String,

    @field:NotBlank(message = "La fecha de publicación no puede estar vacía")
    val fechaDePublicacion: String,

    @field:NotBlank(message = "El estado no puede estar vacío")
    val estado: String,

    @field:NotBlank(message = "El tipo no puede estar vacío")
    val tipo: String,

    @field:Pattern(
        regexp = "^(https?://.+\\..+|/.*)?$",
        message = "La URL debe tener el formato https://sitio.com/imagen.jpg"
    )
    val imagen: String,
)

// DTO para paginar, ordenar y filtrar libros en vista Perfil
data class PaginaLibrosDto(
    val libros: List<LibroTarjetaDto>,
    val totalPaginas: Int
)