package plapstudio.agendify.auth

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import plapstudio.agendify.domain.Turno
import plapstudio.agendify.domain.Usuario
import plapstudio.agendify.errors.ForbiddenException
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.errors.UnauthorizedException
import plapstudio.agendify.repository.AgendaRepository
import plapstudio.agendify.repository.NotificacionRepository
import plapstudio.agendify.repository.PerfilProfesionalRepository
import plapstudio.agendify.repository.ProfesionalAsistenteRepository
import plapstudio.agendify.repository.TurnoRepository
import plapstudio.agendify.repository.UsuarioRepository
import java.util.UUID

@Service
class AuthGuard(
    private val authContext: AuthContext,
    private val usuarioRepository: UsuarioRepository,
    private val agendaRepository: AgendaRepository,
    private val turnoRepository: TurnoRepository,
    private val profesionalRepository: PerfilProfesionalRepository,
    private val asignacionRepository: ProfesionalAsistenteRepository,
    private val notificacionRepository: NotificacionRepository
) {

    fun requireAuthenticated(allowPendingRole: Boolean = false): Usuario {
        val principal = authContext.get() ?: throw UnauthorizedException("Debes iniciar sesion")
        val usuario = usuarioRepository.findByIdOrNull(principal.userId)
            ?: throw UnauthorizedException("La sesion ya no es valida")
        if (!usuario.activo) throw UnauthorizedException("Usuario deshabilitado")
        if (!allowPendingRole && usuario.requiereSeleccionRol()) {
            throw ForbiddenException("Debes seleccionar un rol antes de continuar")
        }
        return usuario
    }

    fun requireAdmin(): Usuario {
        val usuario = requireAuthenticated()
        if (!usuario.esAdmin()) throw ForbiddenException("Acceso restringido a administradores")
        return usuario
    }

    fun requireUser(userId: Long, allowPendingRole: Boolean = false): Usuario {
        val usuario = requireAuthenticated(allowPendingRole)
        if (usuario.esAdmin() || usuario.id == userId) return usuario
        throw ForbiddenException("No puedes acceder a los datos de otro usuario")
    }

    fun requireCliente(clienteId: Long): Usuario {
        val usuario = requireAuthenticated()
        if (usuario.esAdmin()) return usuario
        if (usuario.perfilCliente?.id == clienteId) return usuario
        throw ForbiddenException("Solo el cliente propietario puede realizar esta accion")
    }

    fun requireProfesional(profesionalId: Long): Usuario {
        val usuario = requireAuthenticated()
        if (usuario.esAdmin()) return usuario
        if (usuario.perfilProfesional?.id == profesionalId) return usuario
        throw ForbiddenException("Solo el profesional propietario puede realizar esta accion")
    }

    fun requireProfesionalOrAssignedAssistant(profesionalId: Long): Usuario {
        val usuario = requireAuthenticated()
        if (usuario.esAdmin()) return usuario
        if (usuario.perfilProfesional?.id == profesionalId) return usuario
        if (isAssignedAssistant(usuario, profesionalId)) return usuario
        throw ForbiddenException("No tienes permiso para operar sobre este profesional")
    }

    fun requireAgendaOwner(agendaId: UUID): Usuario {
        val agenda = agendaRepository.findByIdOrNull(agendaId) ?: throw NotFoundException("Agenda no encontrada")
        return requireProfesional(agenda.profesional.id!!)
    }

    fun requireTurnoParticipant(turnoId: UUID): Usuario {
        val turno = turno(turnoId)
        val usuario = requireAuthenticated()
        if (usuario.esAdmin()) return usuario
        if (usuario.perfilCliente?.id == turno.cliente.id) return usuario
        if (usuario.perfilProfesional?.id == turno.agenda.profesional.id) return usuario
        if (isAssignedAssistant(usuario, turno.agenda.profesional.id!!)) return usuario
        throw ForbiddenException("No tienes acceso a este turno")
    }

    fun requireTurnoStaff(turnoId: UUID): Usuario {
        val turno = turno(turnoId)
        return requireProfesionalOrAssignedAssistant(turno.agenda.profesional.id!!)
    }

    fun requireNotificationOwner(notificationId: UUID): Usuario {
        val usuario = requireAuthenticated()
        if (usuario.esAdmin()) return usuario
        val notificacion = notificacionRepository.findByIdOrNull(notificationId)
            ?: throw NotFoundException("Notificacion no encontrada")
        if (notificacion.usuario.id == usuario.id) return usuario
        throw ForbiddenException("No puedes modificar notificaciones de otro usuario")
    }

    fun requireAssignmentManager(assignmentId: UUID): Usuario {
        val usuario = requireAuthenticated()
        if (usuario.esAdmin()) return usuario
        val assignment = asignacionRepository.findByIdOrNull(assignmentId)
            ?: throw NotFoundException("Asignacion no encontrada")
        if (usuario.perfilProfesional?.id == assignment.profesional.id) return usuario
        throw ForbiddenException("No puedes administrar esta asignacion")
    }

    private fun isAssignedAssistant(usuario: Usuario, profesionalId: Long): Boolean {
        if (!usuario.esAsistente()) return false
        val profesional = profesionalRepository.findByIdOrNull(profesionalId) ?: return false
        return asignacionRepository.existsByProfesionalAndAsistente(profesional, usuario)
    }

    private fun turno(turnoId: UUID): Turno =
        turnoRepository.findByIdOrNull(turnoId) ?: throw NotFoundException("Turno no encontrado")
}
