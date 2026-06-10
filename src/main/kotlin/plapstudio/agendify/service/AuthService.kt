package plapstudio.agendify.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import plapstudio.agendify.auth.AuthTokenService
import plapstudio.agendify.auth.GoogleTokenVerifier
import plapstudio.agendify.domain.PerfilCliente
import plapstudio.agendify.domain.PerfilProfesional
import plapstudio.agendify.domain.Usuario
import plapstudio.agendify.dto.AuthResponse
import plapstudio.agendify.dto.GoogleLoginRequest
import plapstudio.agendify.dto.LoginRequest
import plapstudio.agendify.dto.Mapper
import plapstudio.agendify.dto.RegistroRequest
import plapstudio.agendify.dto.SeleccionRolRequest
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
    private val usuarioRepository: UsuarioRepository,
    private val rolRepository: RolRepository,
    private val perfilProfesionalRepository: PerfilProfesionalRepository,
    private val perfilClienteRepository: PerfilClienteRepository,
    private val mapper: Mapper,
    private val authTokenService: AuthTokenService,
    private val googleTokenVerifier: GoogleTokenVerifier
) {

    fun login(req: LoginRequest): AuthResponse {
        val usuario = usuarioRepository.findByEmail(req.email.trim().lowercase())
            ?: throw UnauthorizedException("Credenciales invalidas")
        if (usuario.contrasenaHash != req.password) {
            throw UnauthorizedException("Credenciales invalidas")
        }
        if (!usuario.activo) throw UnauthorizedException("Usuario deshabilitado")
        return buildAuthResponse(usuario)
    }

    @Transactional
    fun registrar(req: RegistroRequest): AuthResponse {
        val email = req.email.trim().lowercase()
        if (usuarioRepository.existsByEmail(email)) {
            throw ConflictException("Ya existe un usuario con ese email")
        }

        val usuario = usuarioRepository.save(
            Usuario(
                email = email,
                contrasenaHash = req.password,
                nombreCompleto = req.nombreCompleto.trim(),
                telefono = req.telefono.trim(),
                roles = mutableSetOf(resolveAllowedRole(req.rol))
            )
        )

        ensureProfilesForRole(
            usuario = usuario,
            rolNombre = req.rol.uppercase(),
            especialidad = req.especialidad,
            biografia = req.biografia,
            localidad = req.localidad,
            direccion = req.direccion,
            precio = req.precio,
            servicios = req.servicios
        )
        return buildAuthResponse(usuario)
    }

    @Transactional
    fun loginConGoogle(req: GoogleLoginRequest): AuthResponse {
        if (req.credential.isBlank()) throw UnauthorizedException("Falta la credencial de Google")

        val identity = googleTokenVerifier.verify(req.credential)
        val usuario = usuarioRepository.findByGoogleSub(identity.sub)
            ?: usuarioRepository.findByEmail(identity.email)?.also { existente ->
                if (existente.googleSub != null && existente.googleSub != identity.sub) {
                    throw ConflictException("Ese email ya esta vinculado a otra cuenta de Google")
                }
                existente.googleSub = identity.sub
            }
            ?: createGoogleUser(identity.email, identity.nombreCompleto, identity.sub)

        if (!usuario.activo) throw UnauthorizedException("Usuario deshabilitado")
        if (usuario.googleSub == null) {
            usuario.googleSub = identity.sub
        }
        if (usuario.nombreCompleto.isBlank()) {
            usuario.nombreCompleto = identity.nombreCompleto
        }
        return buildAuthResponse(usuarioRepository.save(usuario))
    }

    @Transactional(readOnly = true)
    fun me(usuarioId: Long): AuthResponse {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { UnauthorizedException("La sesion ya no es valida") }
        if (!usuario.activo) throw UnauthorizedException("Usuario deshabilitado")
        return buildAuthResponse(usuario)
    }

    @Transactional
    fun seleccionarRol(usuarioId: Long, req: SeleccionRolRequest): AuthResponse {
        val usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow { UnauthorizedException("La sesion ya no es valida") }

        val rolNombre = req.rol.uppercase()
        val rol = resolveAllowedRole(rolNombre)
        val yaDefinido = usuario.roles.map { it.nombre }.firstOrNull { it != "SIN_DEFINIR" }
        if (yaDefinido != null && yaDefinido != rolNombre) {
            throw ConflictException("El usuario ya tiene un rol definido")
        }

        usuario.roles.removeIf { it.nombre == "SIN_DEFINIR" }
        usuario.roles.removeIf { it.nombre in setOf("CLIENTE", "PROFESIONAL", "ASISTENTE") && it.nombre != rolNombre }
        usuario.roles.add(rol)

        ensureProfilesForRole(
            usuario = usuario,
            rolNombre = rolNombre,
            especialidad = req.especialidad,
            biografia = req.biografia,
            localidad = req.localidad,
            direccion = req.direccion,
            precio = req.precio,
            servicios = req.servicios
        )
        return buildAuthResponse(usuarioRepository.save(usuario))
    }

    private fun buildAuthResponse(usuario: Usuario): AuthResponse =
        AuthResponse(
            token = authTokenService.issueToken(usuario.id!!),
            usuario = mapper.toUsuarioDto(usuario)
        )

    private fun resolveAllowedRole(rolNombre: String) =
        if (rolNombre !in setOf("CLIENTE", "PROFESIONAL", "ASISTENTE")) {
            throw BusinessException("Rol invalido. Usar CLIENTE, PROFESIONAL o ASISTENTE")
        } else {
            rolRepository.findByNombre(rolNombre)
                ?: throw NotFoundException("Rol no encontrado: $rolNombre")
        }

    private fun createGoogleUser(email: String, nombreCompleto: String, googleSub: String): Usuario {
        val rolPendiente = rolRepository.findByNombre("SIN_DEFINIR")
            ?: throw NotFoundException("Rol no encontrado: SIN_DEFINIR")
        return usuarioRepository.save(
            Usuario(
                email = email,
                contrasenaHash = "",
                nombreCompleto = nombreCompleto.trim(),
                telefono = "",
                googleSub = googleSub,
                roles = mutableSetOf(rolPendiente)
            )
        )
    }

    private fun ensureProfilesForRole(
        usuario: Usuario,
        rolNombre: String,
        especialidad: String?,
        biografia: String? = null,
        localidad: String? = null,
        direccion: String? = null,
        precio: BigDecimal? = null,
        servicios: List<String>? = null
    ) {
        when (rolNombre) {
            "CLIENTE" -> if (usuario.perfilCliente == null) {
                val perfil = PerfilCliente(usuario = usuario)
                perfilClienteRepository.save(perfil)
                usuario.perfilCliente = perfil
            }

            "PROFESIONAL" -> {
                val especialidadNormalizada = especialidad?.trim().orEmpty()
                val serviciosNormalizados = servicios.orEmpty()
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .ifEmpty {
                        if (especialidadNormalizada.isNotBlank()) listOf(especialidadNormalizada) else emptyList()
                    }
                val perfil = usuario.perfilProfesional
                if (perfil == null) {
                    val nuevoPerfil = PerfilProfesional(
                        usuario = usuario,
                        especialidad = especialidadNormalizada,
                        biografia = biografia?.trim().orEmpty(),
                        localidad = localidad?.trim().orEmpty(),
                        direccion = direccion?.trim().orEmpty(),
                        precio = precio ?: BigDecimal.ZERO,
                        servicios = serviciosNormalizados.toMutableList()
                    )
                    perfilProfesionalRepository.save(nuevoPerfil)
                    usuario.perfilProfesional = nuevoPerfil
                } else {
                    if (perfil.especialidad.isBlank() && especialidadNormalizada.isNotBlank()) {
                        perfil.especialidad = especialidadNormalizada
                    }
                    if (perfil.biografia.isBlank() && !biografia.isNullOrBlank()) {
                        perfil.biografia = biografia.trim()
                    }
                    if (perfil.localidad.isBlank() && !localidad.isNullOrBlank()) {
                        perfil.localidad = localidad.trim()
                    }
                    if (perfil.direccion.isBlank() && !direccion.isNullOrBlank()) {
                        perfil.direccion = direccion.trim()
                    }
                    if (perfil.precio.compareTo(BigDecimal.ZERO) == 0 && precio != null) {
                        perfil.precio = precio
                    }
                    if (perfil.servicios.isEmpty() && serviciosNormalizados.isNotEmpty()) {
                        perfil.servicios.addAll(serviciosNormalizados)
                    }
                }
            }
        }
    }
}
