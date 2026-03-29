package ar.edu.unsam.phm.domain

class Resenia(
    val usuario: Usuario,
    var libro: Libro,
    var puntaje: Int,
    var comentario: String
) : Registrable {
    override var id: Int? = null

    init {
        require(puntaje in 1..5) { "El puntaje debe ser entre 1 y 5" }
        require(comentario.isNotBlank()) { "El comentario no puede estar vacio" }
    }

    fun actualizar(nuevoPuntaje: Int, nuevoComentario: String) {
        require(nuevoPuntaje in 1..5) { "El puntaje debe ser entre 1 y 5" }
        require(nuevoComentario.isNotBlank()) { "El comentario no puede estar vacio" }

        puntaje = nuevoPuntaje
        comentario = nuevoComentario
    }
}
