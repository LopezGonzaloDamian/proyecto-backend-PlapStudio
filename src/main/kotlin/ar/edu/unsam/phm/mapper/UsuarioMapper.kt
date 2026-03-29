package ar.edu.unsam.phm.mapper

import ar.edu.unsam.phm.domain.TipoDeUsuario
import ar.edu.unsam.phm.domain.Usuario
import ar.edu.unsam.phm.dto.DuenioDto
import ar.edu.unsam.phm.dto.UsuarioResponse

fun usuarioToDto(usuario: Usuario, librosLeidos: Int = 0 , librosPrestados: Int = 0): UsuarioResponse {
    return UsuarioResponse(
        nombre = usuario.nombre,
        apellido = usuario.apellido,
        email = usuario.email,
        username = usuario.username,
        avatar = usuario.avatar,
        celular = usuario.celular,
        ciudad = usuario.ciudad,
        descripcion = usuario.descripcion,
        bibliokarmas = usuario.bibliokarmas,
        fechaDeAlta = usuario.fechaDeAlta,
        lector = usuario.tipoUsuario == TipoDeUsuario.lector ||
                usuario.tipoUsuario == TipoDeUsuario.lector_publicador,
        publicador = usuario.tipoUsuario == TipoDeUsuario.publicador ||
                usuario.tipoUsuario == TipoDeUsuario.lector_publicador,
        lectorPublicador = usuario.tipoUsuario == TipoDeUsuario.lector_publicador,
        librosPrestados = librosPrestados,
        librosLeidos = librosLeidos
    )
}

fun Usuario.toDuenioDto() = DuenioDto(
    id       = requireNotNull(this.id),
    nombre   = this.nombre,
    apellido = this.apellido,
    username = this.username,
)