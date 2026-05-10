package plapstudio.agendify.controller

import org.springframework.web.bind.annotation.*
import plapstudio.agendify.dto.LoginRequest
import plapstudio.agendify.dto.Mapper
import plapstudio.agendify.dto.RegistroRequest
import plapstudio.agendify.dto.UsuarioDto
import plapstudio.agendify.service.AuthService

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
class AuthController(
    private val authService: AuthService,
    private val mapper:      Mapper
) {

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): UsuarioDto =
        mapper.toUsuarioDto(authService.login(req))

    @PostMapping("/registro")
    fun registro(@RequestBody req: RegistroRequest): UsuarioDto =
        mapper.toUsuarioDto(authService.registrar(req))
}
