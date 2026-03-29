package plapstudio.agendify.service

import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier
import plapstudio.agendify.domain.*
import plapstudio.agendify.dto.UsuarioActualizarDto
import plapstudio.agendify.repository.RepositorioUsuarios

@Component
class UsuarioService(
    @Qualifier("repoUsuarios") private val repoUsuarios: RepositorioUsuarios,
) {
    fun actualizarUsuario(id: Int, dto: UsuarioActualizarDto) {
        require(dto.nombre.isNotBlank()) { "El nombre no puede estar vacÃƒÂ­o" }
        require(dto.apellido.isNotBlank()) { "El apellido no puede estar vacÃƒÂ­o" }
        require(dto.celular.isNotBlank()) { "El celular no puede estar vacÃƒÂ­o" }
        require(dto.celular.all { it.isDigit() }) { "El celular debe contener solo nÃƒÂºmeros" }
        require(dto.celular.length <= 10) { "El celular no puede tener mÃƒÂ¡s de 10 cifras" }
        require(dto.ciudad.isNotBlank()) { "La ciudad no puede estar vacÃƒÂ­a" }
        require(dto.lector || dto.publicador) { "El usuario debe ser al menos lector o publicador" }

        val usuario = repoUsuarios.getById(id)

        usuario.nombre = dto.nombre.trim()
        usuario.apellido = dto.apellido.trim()
        usuario.descripcion = dto.descripcion.trim()
        usuario.celular = dto.celular.trim()
        usuario.ciudad = dto.ciudad.trim()
        usuario.avatar = dto.avatar
        usuario.tipoUsuario = when {
            dto.lector && dto.publicador -> TipoDeUsuario.lector_publicador
            dto.lector -> TipoDeUsuario.lector
            else -> TipoDeUsuario.publicador
        }

        repoUsuarios.update(usuario)
    }

    fun getById(id: Int) = repoUsuarios.getById(id)

    fun existeUsername(username: String) = repoUsuarios.existeUsername(username)

    fun existeEmail(email: String) = repoUsuarios.buscarPorEmail(email) != null

    fun create(usuario: Usuario) = repoUsuarios.create(usuario)

    fun obtenerPorEmail(email: String): Usuario? = repoUsuarios.buscarPorEmail(email)
}