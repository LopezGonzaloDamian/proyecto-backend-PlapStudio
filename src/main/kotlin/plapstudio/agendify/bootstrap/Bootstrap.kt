package plapstudio.agendify.bootstrap

import plapstudio.agendify.domain.*
import plapstudio.agendify.repository.*
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class Bootstrap : InitializingBean {

    @Autowired private lateinit var usuarioRepository: UsuarioRepository
    @Autowired private lateinit var agendaRepository:  AgendaRepository
    @Autowired private lateinit var turnoRepository:   TurnoRepository

    override fun afterPropertiesSet() {
        if (usuarioRepository.count() > 0) return

        // ============================================================
        // USUARIOS
        // ============================================================

        val drLopez = usuarioRepository.save(Usuario(
            nombre   = "Gonzalo", apellido = "López",
            email    = "gonzalo.lopez@agendify.com",
            password = "1234",   telefono = "1155667788",
            rol      = Rol.PROFESIONAL
        ))

        val draGomez = usuarioRepository.save(Usuario(
            nombre   = "María",  apellido = "Gómez",
            email    = "maria.gomez@agendify.com",
            password = "1234",   telefono = "1144556677",
            rol      = Rol.PROFESIONAL
        ))

        val drRamirez = usuarioRepository.save(Usuario(
            nombre   = "Carlos", apellido = "Ramírez",
            email    = "carlos.ramirez@agendify.com",
            password = "1234",   telefono = "1133445566",
            rol      = Rol.PROFESIONAL
        ))

        val asistente = usuarioRepository.save(Usuario(
            nombre   = "Rodrigo", apellido = "Casco",
            email    = "rodrigo.casco@agendify.com",
            password = "1234",    telefono = "1122334455",
            rol      = Rol.ASISTENTE
        ))

        val admin = usuarioRepository.save(Usuario(
            nombre   = "Admin",   apellido = "Agendify",
            email    = "admin@agendify.com",
            password = "admin1234", telefono = "1100000000",
            rol      = Rol.ADMIN
        ))

        val santiago = usuarioRepository.save(Usuario(
            nombre   = "Santiago", apellido = "Zolla",
            email    = "santiago.zolla@agendify.com",
            password = "1234",    telefono = "1111223344",
            rol      = Rol.CLIENTE
        ))

        val nahuel = usuarioRepository.save(Usuario(
            nombre   = "Nahuel", apellido = "García",
            email    = "nahuel.garcia@agendify.com",
            password = "1234",   telefono = "1100112233",
            rol      = Rol.CLIENTE
        ))

        val jonathan = usuarioRepository.save(Usuario(
            nombre   = "Jonathan", apellido = "Gómez Ciranna",
            email    = "jonathan.gomez@agendify.com",
            password = "1234",    telefono = "1199887766",
            rol      = Rol.CLIENTE
        ))

        val lucia = usuarioRepository.save(Usuario(
            nombre   = "Lucía", apellido = "Fernández",
            email    = "lucia.fernandez@agendify.com",
            password = "1234",  telefono = "1188776655",
            rol      = Rol.CLIENTE
        ))

        val marcos = usuarioRepository.save(Usuario(
            nombre   = "Marcos", apellido = "Pérez",
            email    = "marcos.perez@agendify.com",
            password = "1234",   telefono = "1177665544",
            rol      = Rol.CLIENTE
        ))

        // ============================================================
        // AGENDAS
        // ============================================================

        val agendaLopez = agendaRepository.save(Agenda(
            nombre                = "Consultorio Dr. López — Clínica Médica",
            descripcion           = "Consultas de clínica médica general y medicina preventiva",
            profesional           = drLopez,
            diasDisponibles       = mutableSetOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            horaInicio            = LocalTime.of(9, 0),
            horaFin               = LocalTime.of(17, 0),
            duracionTurnoMinutos  = 30
        ))

        val agendaGomez = agendaRepository.save(Agenda(
            nombre                = "Consultorio Dra. Gómez — Psicología",
            descripcion           = "Psicología clínica, sesiones individuales y de pareja",
            profesional           = draGomez,
            diasDisponibles       = mutableSetOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            horaInicio            = LocalTime.of(10, 0),
            horaFin               = LocalTime.of(19, 0),
            duracionTurnoMinutos  = 50
        ))

        val agendaRamirez = agendaRepository.save(Agenda(
            nombre                = "Estudio Jurídico Ramírez",
            descripcion           = "Asesoramiento legal en derecho civil y laboral",
            profesional           = drRamirez,
            diasDisponibles       = mutableSetOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
            horaInicio            = LocalTime.of(8, 0),
            horaFin               = LocalTime.of(18, 0),
            duracionTurnoMinutos  = 45
        ))

        val agendaLopezTarde = agendaRepository.save(Agenda(
            nombre                = "Consultorio Dr. López — Tarde",
            descripcion           = "Consultorio externo, sólo turnos tarde",
            profesional           = drLopez,
            diasDisponibles       = mutableSetOf(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY),
            horaInicio            = LocalTime.of(14, 0),
            horaFin               = LocalTime.of(20, 0),
            duracionTurnoMinutos  = 30
        ))

        // ============================================================
        // TURNOS
        // ============================================================

        // Pasados — COMPLETADOS
        turnoRepository.save(Turno(
            agenda    = agendaLopez,
            cliente   = santiago,
            fechaHora = LocalDateTime.of(2026, 4, 6, 9, 0),   // Lunes pasado
            estado    = EstadoTurno.COMPLETADO,
            pagado    = true
        ))

        turnoRepository.save(Turno(
            agenda    = agendaGomez,
            cliente   = lucia,
            fechaHora = LocalDateTime.of(2026, 4, 7, 10, 0),  // Martes pasado
            estado    = EstadoTurno.COMPLETADO,
            pagado    = true
        ))

        turnoRepository.save(Turno(
            agenda    = agendaRamirez,
            cliente   = nahuel,
            fechaHora = LocalDateTime.of(2026, 4, 8, 8, 0),   // Miércoles pasado
            estado    = EstadoTurno.COMPLETADO,
            pagado    = true
        ))

        // Pasado — CANCELADO
        turnoRepository.save(Turno(
            agenda    = agendaLopez,
            cliente   = marcos,
            fechaHora = LocalDateTime.of(2026, 4, 6, 9, 30),
            estado    = EstadoTurno.CANCELADO
        ))

        // Próximos — CONFIRMADOS
        turnoRepository.save(Turno(
            agenda    = agendaLopez,
            cliente   = santiago,
            fechaHora = LocalDateTime.of(2026, 4, 15, 9, 0),  // Miércoles próximo
            estado    = EstadoTurno.CONFIRMADO
        ))

        turnoRepository.save(Turno(
            agenda    = agendaLopez,
            cliente   = nahuel,
            fechaHora = LocalDateTime.of(2026, 4, 15, 9, 30),
            estado    = EstadoTurno.CONFIRMADO
        ))

        turnoRepository.save(Turno(
            agenda    = agendaGomez,
            cliente   = jonathan,
            fechaHora = LocalDateTime.of(2026, 4, 16, 10, 0), // Jueves próximo
            estado    = EstadoTurno.CONFIRMADO
        ))

        turnoRepository.save(Turno(
            agenda    = agendaLopezTarde,
            cliente   = lucia,
            fechaHora = LocalDateTime.of(2026, 4, 16, 14, 0),
            estado    = EstadoTurno.CONFIRMADO
        ))

        // Próximos — PENDIENTES (sin confirmar aún)
        turnoRepository.save(Turno(
            agenda    = agendaGomez,
            cliente   = marcos,
            fechaHora = LocalDateTime.of(2026, 4, 16, 10, 50),
            estado    = EstadoTurno.PENDIENTE
        ))

        turnoRepository.save(Turno(
            agenda    = agendaRamirez,
            cliente   = santiago,
            fechaHora = LocalDateTime.of(2026, 4, 17, 8, 45),  // Viernes
            estado    = EstadoTurno.PENDIENTE
        ))

        turnoRepository.save(Turno(
            agenda    = agendaRamirez,
            cliente   = nahuel,
            fechaHora = LocalDateTime.of(2026, 4, 20, 9, 30),  // Lunes siguiente
            estado    = EstadoTurno.PENDIENTE
        ))
    }
}
