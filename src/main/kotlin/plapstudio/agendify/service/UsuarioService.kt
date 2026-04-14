package plapstudio.agendify.service

import plapstudio.agendify.domain.Usuario
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.UsuarioRepository
import org.springframework.stereotype.Service

@Service
class UsuarioService(private val usuarioRepository: UsuarioRepository) {

    fun findById(id: Long): Usuario =
        usuarioRepository.findById(id).orElseThrow { NotFoundException("Usuario no encontrado con id: $id") }

    fun findAll(): List<Usuario> = usuarioRepository.findAll()

    fun create(usuario: Usuario): Usuario = usuarioRepository.save(usuario)

    fun update(id: Long, datos: Usuario): Usuario {
        val existente            = findById(id)
        existente.nombreCompleto = datos.nombreCompleto
        existente.telefono       = datos.telefono
        existente.activo         = datos.activo
        existente.roles          = datos.roles
        return usuarioRepository.save(existente)
    }

    fun delete(id: Long) {
        findById(id)
        usuarioRepository.deleteById(id)
    }
}
