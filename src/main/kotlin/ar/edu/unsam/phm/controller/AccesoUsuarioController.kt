package ar.edu.unsam.phm.controller

import ar.edu.unsam.phm.dto.NuevoUsuarioDTO
import ar.edu.unsam.phm.dto.UsuarioLoginRequest
import ar.edu.unsam.phm.dto.UsuarioLoginResponse
import ar.edu.unsam.phm.service.UsuarioLoginService
import ar.edu.unsam.phm.service.UsuarioRegistroService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/usuario")
@CrossOrigin("*")
class AccesoUsuarioController2(
    private val loginService: UsuarioLoginService,
    private val registroService: UsuarioRegistroService
) {
    @PostMapping("/login")
    fun login(@RequestBody req: UsuarioLoginRequest): UsuarioLoginResponse =
        loginService.autenticar(req)

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED) // 201
    fun registro(@RequestBody dto: NuevoUsuarioDTO): UsuarioLoginResponse {
        val usuario = registroService.crearUsuarioPorDefecto(dto)
        return UsuarioLoginResponse(
            idUsuario = requireNotNull(usuario.id),
            avatarImg = usuario.avatar
        )
    }
}