package plapstudio.agendify.repository

import plapstudio.agendify.domain.Notificacion
import plapstudio.agendify.domain.Usuario
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface NotificacionRepository : JpaRepository<Notificacion, UUID> {
    fun findByUsuario(usuario: Usuario): List<Notificacion>
    fun findByUsuarioAndLeidaFalse(usuario: Usuario): List<Notificacion>
}
