package plapstudio.agendify.controller

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import plapstudio.agendify.dto.ActivarRolRequest
import plapstudio.agendify.auth.AuthGuard
import plapstudio.agendify.dto.AuthResponse
import plapstudio.agendify.dto.GoogleLoginRequest
import plapstudio.agendify.dto.LoginRequest
import plapstudio.agendify.dto.RegistroRequest
import plapstudio.agendify.dto.SeleccionRolRequest
import plapstudio.agendify.service.AuthService

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
class AuthController(
    private val authService: AuthService,
    private val authGuard: AuthGuard
) {

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): AuthResponse =
        authService.login(req)

    @PostMapping("/registro")
    fun registro(@Valid @RequestBody req: RegistroRequest): AuthResponse =
        authService.registrar(req)

    @PostMapping("/google")
    fun google(@Valid @RequestBody req: GoogleLoginRequest): AuthResponse =
        authService.loginConGoogle(req)

    @GetMapping("/me")
    fun me(): AuthResponse =
        authService.me(authGuard.requireAuthenticated(allowPendingRole = true).id!!)

    @PostMapping("/select-role")
    fun selectRole(@Valid @RequestBody req: SeleccionRolRequest): AuthResponse =
        authService.seleccionarRol(authGuard.requireAuthenticated(allowPendingRole = true).id!!, req)

    @PostMapping("/activate-role")
    fun activateRole(@Valid @RequestBody req: ActivarRolRequest): AuthResponse =
        authService.activarRol(authGuard.requireAuthenticated().id!!, req)
}
