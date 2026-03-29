package ar.edu.unsam.phm.dto

import ar.edu.unsam.phm.domain.Resenia

data class ReseniaDetalleDto(
    val id: Int,
    val usuario: String,
    val imagen: String,
    val puntuacion: Int,
    val comentario: String
)

fun Resenia.toReseniaDetalleDto(): ReseniaDetalleDto {
    return ReseniaDetalleDto(
        id = this.id!!,
        usuario = "${usuario.nombre} ${usuario.apellido}",
        imagen = usuario.avatar,
        puntuacion = this.puntaje,
        comentario = this.comentario
    )
}
