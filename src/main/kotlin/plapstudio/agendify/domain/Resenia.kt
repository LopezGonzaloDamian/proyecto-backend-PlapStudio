package plapstudio.agendify.domain

data class Resenia(
    var usuario: Usuario = Usuario(),
    var libro: Libro = Libro(),
    var puntaje: Int = 0
) : Registrable()
