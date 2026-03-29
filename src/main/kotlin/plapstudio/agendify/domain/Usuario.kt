package plapstudio.agendify.domain

data class Usuario(
    var nombre: String = "",
    var apellido: String = "",
    var username: String = "",
    var email: String = ""
) : Registrable()
