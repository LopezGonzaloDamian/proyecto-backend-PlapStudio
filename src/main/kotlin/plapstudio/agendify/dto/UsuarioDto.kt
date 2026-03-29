package plapstudio.agendify.dto

import java.time.LocalDate

data class UsuarioResponse(
    var nombre:      String,
    var apellido:    String,
    val email:       String,
    val username:    String,
    var avatar:      String,
    var celular:     String,
    var ciudad:      String,
    var descripcion: String,
    var bibliokarmas:     Int,
    val fechaDeAlta:      LocalDate,
    val lector:           Boolean,
    val publicador:       Boolean,
    val lectorPublicador: Boolean,
    val librosPrestados:  Int,
    val librosLeidos:     Int,
)

data class DuenioDto(
    val id:       Int,
    val nombre:   String,
    val apellido: String,
    val username: String,
)

data class UsuarioLoginRequest(
    val email:    String,
    val password: String
)

data class UsuarioLoginResponse(
    val idUsuario: Int,
    val avatarImg: String? = null
)

data class NuevoUsuarioDTO(
    val nombre:   String,
    val apellido: String,
    val email:    String,
    val password: String,
) {
    val usernameSugerido: String
        get() {
            val base = "$nombre $apellido"
                .trim()
                .lowercase()
                .replace("ÃƒÂ¡", "a").replace("ÃƒÂ©", "e").replace("ÃƒÂ­", "i")
                .replace("ÃƒÂ³", "o").replace("ÃƒÂº", "u").replace("ÃƒÂ¼", "u")
                .replace("ÃƒÂ±", "n")
                .replace(Regex("[^a-z0-9\\s]"), "")
                .replace(Regex("\\s+"), "_")
            return base
        }
}

data class UsuarioActualizarDto(
    var nombre:      String,
    var apellido:    String,
    var descripcion: String,
    var celular:     String,
    var ciudad:      String,
    var avatar:      String,
    var lector:      Boolean,
    var publicador:  Boolean
)