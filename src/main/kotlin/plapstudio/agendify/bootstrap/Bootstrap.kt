package plapstudio.agendify.bootstrap

import plapstudio.agendify.domain.*
import plapstudio.agendify.repository.*
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class Bootstrap : InitializingBean {

    @Autowired private lateinit var rolRepository:                  RolRepository
    @Autowired private lateinit var usuarioRepository:              UsuarioRepository
    @Autowired private lateinit var perfilProfesionalRepository:    PerfilProfesionalRepository
    @Autowired private lateinit var perfilClienteRepository:        PerfilClienteRepository
    @Autowired private lateinit var agendaRepository:               AgendaRepository
    @Autowired private lateinit var configuracionHorariaRepository: ConfiguracionHorariaRepository
    @Autowired private lateinit var turnoRepository:                TurnoRepository
    @Autowired private lateinit var pagoRepository:                 PagoRepository

    override fun afterPropertiesSet() {
        if (usuarioRepository.count() > 0) return

        // ============================================================
        // ROLES
        // ============================================================

        val rolAdmin        = rolRepository.save(Rol(nombre = "ADMIN",        descripcion = "Administrador del sistema"))
        val rolProfesional  = rolRepository.save(Rol(nombre = "PROFESIONAL",  descripcion = "Dueño de una o más agendas"))
        val rolAsistente    = rolRepository.save(Rol(nombre = "ASISTENTE",    descripcion = "Colabora en la gestión de una agenda"))
        val rolCliente      = rolRepository.save(Rol(nombre = "CLIENTE",      descripcion = "Reserva turnos en agendas"))

        // ============================================================
        // USUARIOS
        // ============================================================

        val admin = usuarioRepository.save(Usuario(
            email          = "admin@agendify.com",
            contrasenaHash = "1234",
            nombreCompleto = "Admin Agendify",
            telefono       = "1100000000",
            roles          = mutableSetOf(rolAdmin)
        ))

        val drLopez = usuarioRepository.save(Usuario(
            email          = "gonzalo.lopez@agendify.com",
            contrasenaHash = "1234",
            nombreCompleto = "Gonzalo López",
            telefono       = "1155667788",
            roles          = mutableSetOf(rolProfesional)
        ))

        val draGomez = usuarioRepository.save(Usuario(
            email          = "maria.gomez@agendify.com",
            contrasenaHash = "1234",
            nombreCompleto = "María Gómez",
            telefono       = "1144556677",
            roles          = mutableSetOf(rolProfesional)
        ))

        // Profesional que también usa la app como cliente
        val drRamirez = usuarioRepository.save(Usuario(
            email          = "carlos.ramirez@agendify.com",
            contrasenaHash = "1234",
            nombreCompleto = "Carlos Ramírez",
            telefono       = "1133445566",
            roles          = mutableSetOf(rolProfesional, rolCliente)
        ))

        // Profesional que actúa como su propio asistente (negocio chico)
        val draFernandez = usuarioRepository.save(Usuario(
            email          = "laura.fernandez@agendify.com",
            contrasenaHash = "1234",
            nombreCompleto = "Laura Fernández",
            telefono       = "1122334455",
            roles          = mutableSetOf(rolProfesional, rolAsistente)
        ))

        val rodrigo = usuarioRepository.save(Usuario(
            email          = "rodrigo.casco@agendify.com",
            contrasenaHash = "1234",
            nombreCompleto = "Rodrigo Casco",
            telefono       = "1111223344",
            roles          = mutableSetOf(rolAsistente)
        ))

        val santiago = usuarioRepository.save(Usuario(
            email          = "santiago.zolla@agendify.com",
            contrasenaHash = "1234",
            nombreCompleto = "Santiago Zolla",
            telefono       = "1100112233",
            roles          = mutableSetOf(rolCliente)
        ))

        val nahuel = usuarioRepository.save(Usuario(
            email          = "nahuel.garcia@agendify.com",
            contrasenaHash = "1234",
            nombreCompleto = "Nahuel García",
            telefono       = "1199887766",
            roles          = mutableSetOf(rolCliente)
        ))

        val jonathan = usuarioRepository.save(Usuario(
            email          = "jonathan.gomez@agendify.com",
            contrasenaHash = "1234",
            nombreCompleto = "Jonathan Gómez Ciranna",
            telefono       = "1188776655",
            roles          = mutableSetOf(rolCliente)
        ))

        val lucia = usuarioRepository.save(Usuario(
            email          = "lucia.perez@agendify.com",
            contrasenaHash = "1234",
            nombreCompleto = "Lucía Pérez",
            telefono       = "1177665544",
            roles          = mutableSetOf(rolCliente)
        ))

        // drRamirez también es cliente → necesita perfil cliente además de profesional
        val marcosMultiRol = usuarioRepository.save(Usuario(
            email          = "marcos.diaz@agendify.com",
            contrasenaHash = "1234",
            nombreCompleto = "Marcos Díaz",
            telefono       = "1166554433",
            roles          = mutableSetOf(rolProfesional, rolCliente, rolAsistente)
        ))

        // ============================================================
        // PERFILES PROFESIONAL
        // ============================================================

        val perfilLopez = perfilProfesionalRepository.save(PerfilProfesional(
            usuario      = drLopez,
            especialidad = "Clínica Médica",
            biografia    = "Médico clínico con más de 15 años de experiencia en medicina preventiva.",
            urlAvatar    = "https://randomuser.me/api/portraits/men/10.jpg",
            destacado    = true
        ))

        val perfilGomez = perfilProfesionalRepository.save(PerfilProfesional(
            usuario      = draGomez,
            especialidad = "Psicología",
            biografia    = "Psicóloga clínica especializada en terapia cognitivo-conductual.",
            urlAvatar    = "https://randomuser.me/api/portraits/women/10.jpg",
            destacado    = true
        ))

        val perfilRamirez = perfilProfesionalRepository.save(PerfilProfesional(
            usuario      = drRamirez,
            especialidad = "Derecho Civil",
            biografia    = "Abogado con especialización en derecho civil y laboral.",
            urlAvatar    = "https://randomuser.me/api/portraits/men/20.jpg",
            destacado    = false
        ))

        val perfilFernandez = perfilProfesionalRepository.save(PerfilProfesional(
            usuario      = draFernandez,
            especialidad = "Nutrición",
            biografia    = "Nutricionista deportiva. Gestiona su agenda de forma autónoma.",
            urlAvatar    = "https://randomuser.me/api/portraits/women/20.jpg",
            destacado    = false
        ))

        val perfilMarcos = perfilProfesionalRepository.save(PerfilProfesional(
            usuario      = marcosMultiRol,
            especialidad = "Kinesiología",
            biografia    = "Kinesiólogo. También usa la plataforma para sacar sus propios turnos.",
            urlAvatar    = "https://randomuser.me/api/portraits/men/30.jpg",
            destacado    = false
        ))

        // ============================================================
        // PERFILES CLIENTE
        // ============================================================

        val clienteSantiago  = perfilClienteRepository.save(PerfilCliente(usuario = santiago))
        val clienteNahuel    = perfilClienteRepository.save(PerfilCliente(usuario = nahuel))
        val clienteJonathan  = perfilClienteRepository.save(PerfilCliente(usuario = jonathan))
        val clienteLucia     = perfilClienteRepository.save(PerfilCliente(usuario = lucia))
        // drRamirez también tiene perfil cliente (multi-rol)
        val clienteRamirez   = perfilClienteRepository.save(PerfilCliente(usuario = drRamirez, notas = "Paciente con alergia a AINE"))
        val clienteMarcos    = perfilClienteRepository.save(PerfilCliente(usuario = marcosMultiRol))

        // ============================================================
        // AGENDAS
        // ============================================================

        val agendaLopez = agendaRepository.save(Agenda(
            profesional = perfilLopez,
            nombre      = "Consultorio Dr. López — Clínica Médica",
            descripcion = "Consultas de clínica médica general y medicina preventiva"
        ))
        configuracionHorariaRepository.saveAll(listOf(
            ConfiguracionHoraria(agenda = agendaLopez, diaSemana = DayOfWeek.MONDAY,    inicioSlot = LocalTime.of(9, 0),  finSlot = LocalTime.of(17, 0), duracionSlotMinutos = 30),
            ConfiguracionHoraria(agenda = agendaLopez, diaSemana = DayOfWeek.WEDNESDAY, inicioSlot = LocalTime.of(9, 0),  finSlot = LocalTime.of(17, 0), duracionSlotMinutos = 30),
            ConfiguracionHoraria(agenda = agendaLopez, diaSemana = DayOfWeek.FRIDAY,    inicioSlot = LocalTime.of(9, 0),  finSlot = LocalTime.of(13, 0), duracionSlotMinutos = 30)
        ))

        val agendaGomez = agendaRepository.save(Agenda(
            profesional = perfilGomez,
            nombre      = "Consultorio Dra. Gómez — Psicología",
            descripcion = "Sesiones individuales y de pareja"
        ))
        configuracionHorariaRepository.saveAll(listOf(
            ConfiguracionHoraria(agenda = agendaGomez, diaSemana = DayOfWeek.TUESDAY,  inicioSlot = LocalTime.of(10, 0), finSlot = LocalTime.of(19, 0), duracionSlotMinutos = 50),
            ConfiguracionHoraria(agenda = agendaGomez, diaSemana = DayOfWeek.THURSDAY, inicioSlot = LocalTime.of(10, 0), finSlot = LocalTime.of(19, 0), duracionSlotMinutos = 50)
        ))

        val agendaRamirez = agendaRepository.save(Agenda(
            profesional = perfilRamirez,
            nombre      = "Estudio Jurídico Ramírez",
            descripcion = "Asesoramiento legal en derecho civil y laboral"
        ))
        configuracionHorariaRepository.saveAll(listOf(
            ConfiguracionHoraria(agenda = agendaRamirez, diaSemana = DayOfWeek.MONDAY,    inicioSlot = LocalTime.of(8, 0), finSlot = LocalTime.of(18, 0), duracionSlotMinutos = 45),
            ConfiguracionHoraria(agenda = agendaRamirez, diaSemana = DayOfWeek.TUESDAY,   inicioSlot = LocalTime.of(8, 0), finSlot = LocalTime.of(18, 0), duracionSlotMinutos = 45),
            ConfiguracionHoraria(agenda = agendaRamirez, diaSemana = DayOfWeek.WEDNESDAY, inicioSlot = LocalTime.of(8, 0), finSlot = LocalTime.of(18, 0), duracionSlotMinutos = 45),
            ConfiguracionHoraria(agenda = agendaRamirez, diaSemana = DayOfWeek.THURSDAY,  inicioSlot = LocalTime.of(8, 0), finSlot = LocalTime.of(18, 0), duracionSlotMinutos = 45),
            ConfiguracionHoraria(agenda = agendaRamirez, diaSemana = DayOfWeek.FRIDAY,    inicioSlot = LocalTime.of(8, 0), finSlot = LocalTime.of(14, 0), duracionSlotMinutos = 45)
        ))

        val agendaFernandez = agendaRepository.save(Agenda(
            profesional = perfilFernandez,
            nombre      = "Consultoría Nutricional — Dra. Fernández",
            descripcion = "Planes personalizados de alimentación y seguimiento"
        ))
        configuracionHorariaRepository.saveAll(listOf(
            ConfiguracionHoraria(agenda = agendaFernandez, diaSemana = DayOfWeek.MONDAY,    inicioSlot = LocalTime.of(9, 0),  finSlot = LocalTime.of(18, 0), duracionSlotMinutos = 40),
            ConfiguracionHoraria(agenda = agendaFernandez, diaSemana = DayOfWeek.WEDNESDAY, inicioSlot = LocalTime.of(9, 0),  finSlot = LocalTime.of(18, 0), duracionSlotMinutos = 40),
            ConfiguracionHoraria(agenda = agendaFernandez, diaSemana = DayOfWeek.FRIDAY,    inicioSlot = LocalTime.of(9, 0),  finSlot = LocalTime.of(13, 0), duracionSlotMinutos = 40)
        ))

        // ============================================================
        // TURNOS
        // ============================================================

        // Pasados — COMPLETADOS
        val t1 = turnoRepository.save(Turno(
            agenda         = agendaLopez,
            cliente        = clienteSantiago,
            iniciaEn       = LocalDateTime.of(2026, 4, 6, 9, 0),
            duracionMinutos = 30,
            estado         = EstadoTurno.COMPLETADO
        ))
        val t2 = turnoRepository.save(Turno(
            agenda         = agendaGomez,
            cliente        = clienteLucia,
            iniciaEn       = LocalDateTime.of(2026, 4, 8, 10, 0),
            duracionMinutos = 50,
            estado         = EstadoTurno.COMPLETADO
        ))
        val t3 = turnoRepository.save(Turno(
            agenda         = agendaRamirez,
            cliente        = clienteNahuel,
            iniciaEn       = LocalDateTime.of(2026, 4, 7, 8, 0),
            duracionMinutos = 45,
            estado         = EstadoTurno.COMPLETADO
        ))

        // Pasado — CANCELADO
        turnoRepository.save(Turno(
            agenda         = agendaLopez,
            cliente        = clienteJonathan,
            iniciaEn       = LocalDateTime.of(2026, 4, 6, 9, 30),
            duracionMinutos = 30,
            estado         = EstadoTurno.CANCELADO
        ))

        // Próximos — CONFIRMADOS
        turnoRepository.save(Turno(
            agenda         = agendaLopez,
            cliente        = clienteSantiago,
            iniciaEn       = LocalDateTime.of(2026, 4, 15, 9, 0),
            duracionMinutos = 30,
            estado         = EstadoTurno.CONFIRMADO
        ))
        turnoRepository.save(Turno(
            agenda         = agendaLopez,
            cliente        = clienteNahuel,
            iniciaEn       = LocalDateTime.of(2026, 4, 15, 9, 30),
            duracionMinutos = 30,
            estado         = EstadoTurno.CONFIRMADO
        ))
        turnoRepository.save(Turno(
            agenda         = agendaGomez,
            cliente        = clienteJonathan,
            iniciaEn       = LocalDateTime.of(2026, 4, 17, 10, 0),
            duracionMinutos = 50,
            estado         = EstadoTurno.CONFIRMADO
        ))

        // Próximos — PENDIENTES
        turnoRepository.save(Turno(
            agenda         = agendaGomez,
            cliente        = clienteMarcos,
            iniciaEn       = LocalDateTime.of(2026, 4, 17, 10, 50),
            duracionMinutos = 50,
            estado         = EstadoTurno.PENDIENTE
        ))
        turnoRepository.save(Turno(
            agenda         = agendaRamirez,
            cliente        = clienteRamirez,   // profesional reservando como cliente
            iniciaEn       = LocalDateTime.of(2026, 4, 21, 8, 0),
            duracionMinutos = 45,
            estado         = EstadoTurno.PENDIENTE
        ))
        turnoRepository.save(Turno(
            agenda         = agendaFernandez,
            cliente        = clienteSantiago,
            iniciaEn       = LocalDateTime.of(2026, 4, 20, 9, 0),
            duracionMinutos = 40,
            estado         = EstadoTurno.PENDIENTE
        ))

        // ============================================================
        // PAGOS (mock — solo para los turnos completados)
        // ============================================================

        pagoRepository.save(Pago(turno = t1, monto = BigDecimal("3500.00"), estado = EstadoPago.APROBADO, referenciaProveedorMock = "MOCK-001", pagadoEn = t1.iniciaEn.plusMinutes(30)))
        pagoRepository.save(Pago(turno = t2, monto = BigDecimal("5000.00"), estado = EstadoPago.APROBADO, referenciaProveedorMock = "MOCK-002", pagadoEn = t2.iniciaEn.plusMinutes(50)))
        pagoRepository.save(Pago(turno = t3, monto = BigDecimal("4200.00"), estado = EstadoPago.APROBADO, referenciaProveedorMock = "MOCK-003", pagadoEn = t3.iniciaEn.plusMinutes(45)))
    }
}
