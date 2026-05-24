package plapstudio.agendify.service

import plapstudio.agendify.domain.Usuario
import plapstudio.agendify.dto.UsuarioUpdateRequest
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.UsuarioRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UsuarioService(private val usuarioRepository: UsuarioRepository) {

    fun findById(id: Long): Usuario =
        usuarioRepository.findById(id).orElseThrow { NotFoundException("Usuario no encontrado con id: $id") }

    fun findAll(): List<Usuario> = usuarioRepository.findAll()

    fun findByRol(rol: String): List<Usuario> = usuarioRepository.findByRolesNombreIgnoreCase(rol)

    fun create(usuario: Usuario): Usuario = usuarioRepository.save(usuario)

    fun update(id: Long, datos: Usuario): Usuario {
        val existente            = findById(id)
        existente.nombreCompleto = datos.nombreCompleto
        existente.telefono       = datos.telefono
        existente.activo         = datos.activo
        existente.roles          = datos.roles
        return usuarioRepository.save(existente)
    }

    @Transactional
    fun updatePerfil(id: Long, req: UsuarioUpdateRequest): Usuario {
        val existente = findById(id)
        existente.nombreCompleto = req.nombreCompleto.trim()
        existente.telefono = req.telefono.trim()
        existente.urlAvatar = req.urlAvatar.trim()
        return usuarioRepository.save(existente)
    }

    fun delete(id: Long) {
        findById(id)
        usuarioRepository.deleteById(id)
    }
}
