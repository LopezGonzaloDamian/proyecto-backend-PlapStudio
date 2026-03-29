package ar.edu.unsam.phm.service

import ar.edu.unsam.phm.domain.Usuario
import ar.edu.unsam.phm.domain.TipoDeUsuario
import ar.edu.unsam.phm.dto.NuevoUsuarioDTO
import ar.edu.unsam.phm.errors.ConflictException
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class UsuarioRegistroService(
    private val usuarioService: UsuarioService
) {
    fun crearUsuarioPorDefecto(dto: NuevoUsuarioDTO): Usuario {
        if (usuarioService.existeEmail(dto.email)) {
            throw ConflictException(
                "Si el email no está registrado, " +
                        "recibirás un correo para completar el registro."
            )
        }

        val username = generarUsernameUnico(dto.usernameSugerido)

        val usuario = Usuario(
            nombre       = dto.nombre,
            apellido     = dto.apellido,
            email        = dto.email,
            username     = username,
            password     = dto.password,
            avatar       = "",
            celular      = "",
            ciudad       = "",
            descripcion  = "",
            bibliokarmas = 0,
            fechaDeAlta  = LocalDate.now(),
            tipoUsuario  = TipoDeUsuario.lector
        )
        usuarioService.create(usuario)

        return usuario
    }

    private fun generarUsernameUnico(usernameSugerido: String): String {
        if (!usuarioService.existeUsername(usernameSugerido)) {
            return usernameSugerido
        }
        var contador = 2
        while (usuarioService.existeUsername("${usernameSugerido}_$contador")) {
            contador++
        }
        return "${usernameSugerido}_$contador"
    }
}

