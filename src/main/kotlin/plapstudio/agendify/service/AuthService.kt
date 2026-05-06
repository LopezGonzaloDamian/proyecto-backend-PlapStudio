package plapstudio.agendify.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import plapstudio.agendify.domain.PerfilCliente
import plapstudio.agendify.domain.PerfilProfesional
import plapstudio.agendify.domain.Usuario
import plapstudio.agendify.dto.LoginRequest
import plapstudio.agendify.dto.RegistroRequest
import plapstudio.agendify.errors.BusinessException
import plapstudio.agendify.errors.ConflictException
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.errors.UnauthorizedException
import plapstudio.agendify.repository.PerfilClienteRepository
import plapstudio.agendify.repository.PerfilProfesionalRepository
import plapstudio.agendify.repository.RolRepository
import plapstudio.agendify.repository.UsuarioRepository

@Service
class AuthService(
    private val usuarioRepository:           UsuarioRepository,
    private val rolRepository:                RolRepository,
    private val perfilProfesionalRepository:  PerfilProfesionalRepository,
    private val perfilClienteRepository:      PerfilClienteRepository
) {

    fun login(req: LoginRequest): Usuario {
        val usuario = usuarioRepository.findByEmail(req.email.trim().lowercase())
            ?: throw UnauthorizedException("Credenciales inválidas")
        if (usuario.contrasenaHash != req.password) {
            throw UnauthorizedException("Credenciales inválidas")
        }
        if (!usuario.activo) throw UnauthorizedException("Usuario deshabilitado")
        return usuario
    }

    @Transactional
    fun registrar(req: RegistroRequest): Usuario {
        val email = req.email.trim().lowercase()
        if (usuarioRepository.existsByEmail(email)) {
            throw ConflictException("Ya existe un usuario con ese email")
        }
        val rolNombre = req.rol.uppercase()
        if (rolNombre !in setOf("CLIENTE", "PROFESIONAL", "ASISTENTE")) {
            throw BusinessException("Rol inválido. Usar CLIENTE, PROFESIONAL o ASISTENTE")
        }
        val rol = rolRepository.findByNombre(rolNombre)
            ?: throw NotFoundException("Rol no encontrado: $rolNombre")

        val nuevo = Usuario(
            email          = email,
            contrasenaHash = req.password,
            nombreCompleto = req.nombreCompleto.trim(),
            telefono       = req.telefono.trim(),
            roles          = mutableSetOf(rol)
        )
        val usuario = usuarioRepository.save(nuevo)

        when (rolNombre) {
            "CLIENTE" -> {
                val perfil = PerfilCliente(usuario = usuario)
                perfilClienteRepository.save(perfil)
                usuario.perfilCliente = perfil
            }
            "PROFESIONAL" -> {
                val perfil = PerfilProfesional(
                    usuario      = usuario,
                    especialidad = req.especialidad?.trim().orEmpty()
                )
                perfilProfesionalRepository.save(perfil)
                usuario.perfilProfesional = perfil
            }
            // ASISTENTE: solo el usuario, sin perfil específico
        }
        return usuario
    }
}
