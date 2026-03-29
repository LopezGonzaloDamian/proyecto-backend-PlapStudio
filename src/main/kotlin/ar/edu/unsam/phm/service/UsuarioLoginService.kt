package ar.edu.unsam.phm.service

import org.springframework.stereotype.Component
import ar.edu.unsam.phm.dto.UsuarioLoginRequest
import ar.edu.unsam.phm.dto.UsuarioLoginResponse
import ar.edu.unsam.phm.errors.UnauthorizedException

@Component
class UsuarioLoginService(
    private val usuarioService: UsuarioService
) {
    fun autenticar(req: UsuarioLoginRequest): UsuarioLoginResponse {
        val email = req.email.trim()
        val password = req.password

        // Traemos el usuario, si no existe -> 401
        val usuario = usuarioService.obtenerPorEmail(email)
            ?: throw UnauthorizedException()

        // Password incorrecto -> 401
        if (usuario.password != password) throw UnauthorizedException()

        return UsuarioLoginResponse(
            idUsuario = usuario.id!!,
            avatarImg = usuario.avatar
        )
    }
}