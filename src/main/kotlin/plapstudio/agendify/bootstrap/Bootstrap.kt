package plapstudio.agendify.bootstrap

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import plapstudio.agendify.domain.*
import plapstudio.agendify.repository.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

@Service
class Bootstrap {

    @Autowired private lateinit var rolRepository:                  RolRepository
    @Autowired private lateinit var usuarioRepository:              UsuarioRepository
    @Autowired private lateinit var perfilProfesionalRepository:    PerfilProfesionalRepository
    @Autowired private lateinit var perfilClienteRepository:        PerfilClienteRepository
    @Autowired private lateinit var agendaRepository:               AgendaRepository
    @Autowired private lateinit var configuracionHorariaRepository: ConfiguracionHorariaRepository
    @Autowired private lateinit var turnoRepository:                TurnoRepository
    @Autowired private lateinit var pagoRepository:                 PagoRepository
    @Autowired private lateinit var favoritoRepository:             FavoritoRepository
    @Autowired private lateinit var notificacionRepository:         NotificacionRepository
    @Autowired private lateinit var profesionalAsistenteRepository: ProfesionalAsistenteRepository

    private data class ProfTemplate(
        val email: String, val nombre: String, val telefono: String,
        val especialidad: String, val biografia: String,
        val urlAvatar: String, val destacado: Boolean,
        val localidad: String, val direccion: String, val precio: BigDecimal,
        val cobertura: String, val matriculaNacional: String, val matriculaProvincial: String,
        val servicios: List<String>,
        val agendaNombre: String, val agendaDescripcion: String
    )

    private data class ProfRecord(val usuario: Usuario, val perfil: PerfilProfesional, val agenda: Agenda)
    private data class ClienteRecord(val usuario: Usuario, val perfil: PerfilCliente)
    private data class TurnoSeed(
        val agenda: Agenda, val cliente: PerfilCliente,
        val iniciaEn: LocalDateTime, val duracion: Int, val estado: EstadoTurno,
        val notas: String = "", val pagar: Boolean = false
    )

    @EventListener(ApplicationReadyEvent::class)
    @Transactional
    fun load() {
        if (usuarioRepository.count() > 0) return

        // ─── ROLES ──────────────────────────────────────────────────────────
        val rolAdmin       = rolRepository.save(Rol(nombre = "ADMIN",       descripcion = "Administrador del sistema"))
        val rolProfesional = rolRepository.save(Rol(nombre = "PROFESIONAL", descripcion = "Dueño de una o más agendas"))
        val rolAsistente   = rolRepository.save(Rol(nombre = "ASISTENTE",   descripcion = "Colabora en la gestión de una agenda"))
        val rolCliente     = rolRepository.save(Rol(nombre = "CLIENTE",     descripcion = "Reserva turnos en agendas"))
        rolRepository.save(Rol(nombre = "SIN_DEFINIR", descripcion = "Usuario autenticado que todavia no eligio su rol"))

        // ─── ADMIN ──────────────────────────────────────────────────────────
        usuarioRepository.save(Usuario(
            email          = "admin@gmail.com",
            contrasenaHash = "1234",
            nombreCompleto = "Admin Agendify",
            telefono       = "1100000000",
            roles          = mutableSetOf(rolAdmin)
        ))

        // ─── PROFESIONALES ──────────────────────────────────────────────────
        val profesionalesSeed = listOf(
            ProfTemplate(
                email = "martina.rios@gmail.com", nombre = "Martina Rios", telefono = "1139494813",
                especialidad = "Nutrición",
                biografia    = "Atencion nutricional integral para planes de alimentacion, control metabolico y seguimiento de habitos.",
                urlAvatar    = "/img/profesionales/martina-rios.jpg", destacado = true,
                localidad    = "San Andrés", direccion = "Saavedra 2642", precio = BigDecimal("85000"),
                cobertura    = "PARTICULAR", matriculaNacional = "M. N. 130.357", matriculaProvincial = "M. P. 451.624",
                servicios    = listOf("Consulta inicial", "Control mensual", "Plan alimentario"),
                agendaNombre = "Consultorio nutricion - Dra. Martina Rios",
                agendaDescripcion = "Atencion nutricional con turnos cada 45 minutos."
            ),
            ProfTemplate(
                email = "diego.benitez@gmail.com", nombre = "Diego Benitez", telefono = "1139494814",
                especialidad = "Kinesiologia",
                biografia    = "Rehabilitacion fisica, tratamiento de lesiones deportivas y sesiones de movilidad funcional.",
                urlAvatar    = "/img/profesionales/diego-benitez.jpg", destacado = true,
                localidad    = "San Martín", direccion = "Perdriel 2188", precio = BigDecimal("70000"),
                cobertura    = "PARTICULAR", matriculaNacional = "M. N. 130.357", matriculaProvincial = "M. P. 451.624",
                servicios    = listOf("Evaluacion", "Sesion de rehabilitacion", "Masoterapia"),
                agendaNombre = "Consultorio kinesiologico - Lic. Diego Benitez",
                agendaDescripcion = "Sesiones de rehabilitacion y masoterapia."
            ),
            ProfTemplate(
                email = "camila.duarte@gmail.com", nombre = "Camila Duarte", telefono = "1123344556",
                especialidad = "Odontologia",
                biografia    = "Consultorio odontologico con agenda para controles, limpieza, restauraciones y urgencias simples.",
                urlAvatar    = "/img/profesionales/camila-duarte.jpg", destacado = false,
                localidad    = "Villa Martelli", direccion = "Rivadavia 2421", precio = BigDecimal("120000"),
                cobertura    = "PARTICULAR", matriculaNacional = "M. N. 221.840", matriculaProvincial = "M. P. 512.204",
                servicios    = listOf("Control odontologico", "Limpieza dental", "Restauracion"),
                agendaNombre = "Consultorio odontologico - Dra. Camila Duarte",
                agendaDescripcion = "Controles, limpieza y restauraciones dentales."
            ),
            ProfTemplate(
                email = "valeria.sosa@gmail.com", nombre = "Valeria Sosa", telefono = "1198765432",
                especialidad = "Psicologia",
                biografia    = "Acompanamiento psicologico para adultos, ansiedad, organizacion personal y bienestar emocional.",
                urlAvatar    = "/img/profesionales/valeria-sosa.jpg", destacado = false,
                localidad    = "Villa Lynch", direccion = "Vidal 4455", precio = BigDecimal("95000"),
                cobertura    = "PARTICULAR", matriculaNacional = "M. N. 175.912", matriculaProvincial = "M. P. 490.132",
                servicios    = listOf("Primera entrevista", "Sesion individual", "Seguimiento online"),
                agendaNombre = "Consultorio psicologia - Lic. Valeria Sosa",
                agendaDescripcion = "Sesiones individuales y seguimiento online."
            ),
            ProfTemplate(
                email = "leo.barrios@gmail.com", nombre = "Leonel Barrios", telefono = "0981123456",
                especialidad = "Barberia",
                biografia    = "Cortes clasicos y modernos, perfilado de barba y atencion con turnos para evitar esperas.",
                urlAvatar    = "/img/profesionales/leo-barrios.jpg", destacado = false,
                localidad    = "CABA", direccion = "Alcorta 7597", precio = BigDecimal("60000"),
                cobertura    = "", matriculaNacional = "", matriculaProvincial = "",
                servicios    = listOf("Corte clasico", "Corte y barba", "Perfilado de barba"),
                agendaNombre = "Barberia Leo - Lambare",
                agendaDescripcion = "Cortes y barba con turnos cada 45 minutos."
            ),
            ProfTemplate(
                email = "paula.gimenez@gmail.com", nombre = "Paula Gimenez", telefono = "0981456789",
                especialidad = "Peluqueria",
                biografia    = "Turnos para color, brushing, cortes y tratamientos capilares con atencion personalizada.",
                urlAvatar    = "/img/profesionales/paula-gimenez.jpg", destacado = false,
                localidad    = "CABA", direccion = "Alcorta 2544", precio = BigDecimal("110000"),
                cobertura    = "", matriculaNacional = "", matriculaProvincial = "",
                servicios    = listOf("Corte y brushing", "Coloracion", "Tratamiento capilar"),
                agendaNombre = "Peluqueria Paula",
                agendaDescripcion = "Color, brushing y tratamientos capilares."
            ),
            ProfTemplate(
                email = "sofi.acosta@gmail.com", nombre = "Sofia Acosta", telefono = "0981765432",
                especialidad = "Manicurista",
                biografia    = "Agenda de manicura y nail art con turnos programados para esmaltado, kapping y disenos.",
                urlAvatar    = "/img/profesionales/sofi-acosta.jpg", destacado = false,
                localidad    = "Pilar", direccion = "Almafuerte 1315", precio = BigDecimal("75000"),
                cobertura    = "", matriculaNacional = "", matriculaProvincial = "",
                servicios    = listOf("Esmaltado semipermanente", "Kapping", "Nail art"),
                agendaNombre = "Estudio de unas - Sofi",
                agendaDescripcion = "Esmaltado semipermanente, kapping y nail art."
            ),
            ProfTemplate(
                email = "majo.ferreira@gmail.com", nombre = "Micaela Ferreira", telefono = "0981987654",
                especialidad = "Maquillaje profesional",
                biografia    = "Reservas para maquillaje social, novias y producciones con bloques de tiempo definidos.",
                urlAvatar    = "/img/profesionales/majo-ferreira.jpg", destacado = false,
                localidad    = "San Martín", direccion = "Gutiérrez 1234", precio = BigDecimal("130000"),
                cobertura    = "", matriculaNacional = "", matriculaProvincial = "",
                servicios    = listOf("Maquillaje social", "Maquillaje para eventos", "Prueba de novia"),
                agendaNombre = "Estudio de maquillaje Majo",
                agendaDescripcion = "Maquillaje social y para eventos."
            )
        )

        val profesionales = profesionalesSeed.map { t -> crearProfesional(t, rolProfesional) }

        val martina = profesionales[0]
        val diego   = profesionales[1]
        val camila  = profesionales[2]
        val valeria = profesionales[3]
        val leo     = profesionales[4]
        val paula   = profesionales[5]
        val sofi    = profesionales[6]
        val majo    = profesionales[7]

        // ─── ASISTENTES ─────────────────────────────────────────────────────
        val luciaG = usuarioRepository.save(Usuario(
            email          = "lucia.gomez@gmail.com",
            contrasenaHash = "1234",
            nombreCompleto = "Lucia Gomez",
            telefono       = "1100002233",
            roles          = mutableSetOf(rolAsistente)
        ))
        val rodrigo = usuarioRepository.save(Usuario(
            email          = "rodrigo.casco@gmail.com",
            contrasenaHash = "1234",
            nombreCompleto = "Rodrigo Casco",
            telefono       = "1100003344",
            roles          = mutableSetOf(rolAsistente)
        ))

        profesionalAsistenteRepository.save(ProfesionalAsistente(profesional = martina.perfil, asistente = luciaG, estado = EstadoAsignacionAsistente.ACEPTADA))
        profesionalAsistenteRepository.save(ProfesionalAsistente(profesional = leo.perfil,     asistente = luciaG, estado = EstadoAsignacionAsistente.ACEPTADA))
        profesionalAsistenteRepository.save(ProfesionalAsistente(profesional = sofi.perfil,    asistente = luciaG, estado = EstadoAsignacionAsistente.ACEPTADA))
        profesionalAsistenteRepository.save(ProfesionalAsistente(profesional = diego.perfil,   asistente = rodrigo, estado = EstadoAsignacionAsistente.ACEPTADA))

        // ─── CLIENTES ───────────────────────────────────────────────────────
        val clientesSeed = listOf(
            "ana.garcia@gmail.com"     to ("Ana Garcia"     to "+595 981 111 111"),
            "carlos.lopez@gmail.com"   to ("Carlos Lopez"   to "+595 981 222 222"),
            "marta.benitez@gmail.com"  to ("Marta Benitez"  to "+595 981 333 333"),
            "lucia.peralta@gmail.com"  to ("Lucia Peralta"  to "+595 981 444 444"),
            "santiago.zolla@gmail.com" to ("Santiago Zolla" to "+595 981 555 555"),
            "nahuel.garcia@gmail.com"  to ("Nahuel Garcia"  to "+595 981 666 666")
        )
        val clientes = clientesSeed.map { (email, datos) ->
            val (nombre, telefono) = datos
            val u = usuarioRepository.save(Usuario(
                email          = email,
                contrasenaHash = "1234",
                nombreCompleto = nombre,
                telefono       = telefono,
                roles          = mutableSetOf(rolCliente)
            ))
            ClienteRecord(u, perfilClienteRepository.save(PerfilCliente(usuario = u)))
        }
        val ana    = clientes[0]
        val carlos = clientes[1]
        val marta  = clientes[2]
        val luciaP = clientes[3]
        val santi  = clientes[4]
        val nahuel = clientes[5]

        // ─── FAVORITOS ──────────────────────────────────────────────────────
        favoritoRepository.save(Favorito(cliente = santi.perfil,  profesional = martina.perfil))
        favoritoRepository.save(Favorito(cliente = santi.perfil,  profesional = valeria.perfil))
        favoritoRepository.save(Favorito(cliente = ana.perfil,    profesional = martina.perfil))
        favoritoRepository.save(Favorito(cliente = carlos.perfil, profesional = leo.perfil))

        // ─── TURNOS ─────────────────────────────────────────────────────────
        val turnosSeed = listOf(
            // Cliente santi
            TurnoSeed(martina.agenda, santi.perfil,  LocalDateTime.of(2026, 4, 22, 10, 30), 45, EstadoTurno.CONFIRMADO, "Llevar estudios recientes."),
            TurnoSeed(diego.agenda,   santi.perfil,  LocalDateTime.of(2026, 4, 18, 14, 30), 45, EstadoTurno.CONFIRMADO),
            TurnoSeed(camila.agenda,  santi.perfil,  LocalDateTime.of(2026, 4, 10,  9, 30), 45, EstadoTurno.CONFIRMADO, pagar = true),
            TurnoSeed(leo.agenda,     santi.perfil,  LocalDateTime.of(2026, 4, 18, 19,  0), 45, EstadoTurno.CONFIRMADO),

            // Profesional dashboard (Martina)
            TurnoSeed(martina.agenda, ana.perfil,    LocalDateTime.of(2026, 4, 18,  9,  0), 45, EstadoTurno.CONFIRMADO, "Evaluacion inicial", pagar = true),
            TurnoSeed(martina.agenda, carlos.perfil, LocalDateTime.of(2026, 4, 18, 10, 30), 45, EstadoTurno.CONFIRMADO,  "Sesion de seguimiento"),
            TurnoSeed(martina.agenda, marta.perfil,  LocalDateTime.of(2026, 4, 19, 15,  0), 45, EstadoTurno.CONFIRMADO, "Control mensual", pagar = true),

            // Asistente dashboard (Lucia Gomez)
            TurnoSeed(martina.agenda, ana.perfil,    LocalDateTime.of(2026, 5, 2,  9,  0), 45, EstadoTurno.CONFIRMADO, "Recordar plan anterior."),
            TurnoSeed(leo.agenda,     carlos.perfil, LocalDateTime.of(2026, 5, 2, 11, 30), 45, EstadoTurno.CONFIRMADO,  "Corte y barba."),
            TurnoSeed(sofi.agenda,    luciaP.perfil, LocalDateTime.of(2026, 5, 4, 17, 30), 45, EstadoTurno.CONFIRMADO, "Kapping con esmalte nude."),
            TurnoSeed(sofi.agenda,    marta.perfil,  LocalDateTime.of(2026, 5, 3, 15,  0), 45, EstadoTurno.CANCELADO,  "La clienta aviso que no llegaba a tiempo."),

            // Extras
            TurnoSeed(valeria.agenda, nahuel.perfil, LocalDateTime.of(2026, 4, 20, 13, 30), 45, EstadoTurno.CONFIRMADO),
            TurnoSeed(paula.agenda,   ana.perfil,    LocalDateTime.of(2026, 4, 21, 11,  0), 45, EstadoTurno.CONFIRMADO),
            TurnoSeed(majo.agenda,    luciaP.perfil, LocalDateTime.of(2026, 4, 24, 16,  0), 45, EstadoTurno.CONFIRMADO, "Maquillaje social", pagar = true)
        )

        turnosSeed.forEach { t ->
            val turno = turnoRepository.save(Turno(
                agenda          = t.agenda,
                cliente         = t.cliente,
                iniciaEn        = t.iniciaEn,
                duracionMinutos = t.duracion,
                estado          = t.estado,
                notas           = t.notas
            ))
            val precio = t.agenda.profesional.precio
            if (t.pagar) {
                val porcentajeComision = BigDecimal("5.00")
                val montoComision = precio.multiply(porcentajeComision)
                    .divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
                    .setScale(0, RoundingMode.HALF_UP)
                pagoRepository.save(Pago(
                    turno                   = turno,
                    monto                   = precio,
                    porcentajeComision      = porcentajeComision,
                    montoComision           = montoComision,
                    estado                  = EstadoPago.APROBADO,
                    origen                  = OrigenPago.ONLINE,
                    referenciaProveedorMock = "MOCK-${turno.id}",
                    pagadoEn                = t.iniciaEn
                ))
            } else if (precio > BigDecimal.ZERO && t.estado != EstadoTurno.CANCELADO) {
                pagoRepository.save(Pago(
                    turno              = turno,
                    monto              = precio,
                    porcentajeComision = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    montoComision      = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    estado             = EstadoPago.PENDIENTE,
                    origen             = OrigenPago.EXTERNO
                ))
            }
        }

        // ─── NOTIFICACIONES ─────────────────────────────────────────────────
        notificacionRepository.save(Notificacion(
            usuario = santi.usuario, canal = "IN_APP",
            titulo  = "Recordatorio de turno",
            cuerpo  = "Tenes un turno confirmado con Dra. Martina Rios el 22/04 a las 10:30."
        ))
        notificacionRepository.save(Notificacion(
            usuario = santi.usuario, canal = "IN_APP",
            titulo  = "Pago pendiente",
            cuerpo  = "El turno de nutricion tiene un pago online disponible."
        ))
        notificacionRepository.save(Notificacion(
            usuario = martina.usuario, canal = "IN_APP",
            titulo  = "Nuevo turno reservado",
            cuerpo  = "Carlos Lopez reservo una sesion para hoy a las 10:30."
        ))
        notificacionRepository.save(Notificacion(
            usuario = luciaG, canal = "IN_APP",
            titulo  = "Turno cancelado",
            cuerpo  = "Marta Benitez cancelo su turno del 03/05 a las 15:00."
        ))
    }

    private fun crearProfesional(t: ProfTemplate, rolProfesional: Rol): ProfRecord {
        val u = usuarioRepository.save(Usuario(
            email          = t.email,
            contrasenaHash = "1234",
            nombreCompleto = t.nombre,
            telefono       = t.telefono,
            roles          = mutableSetOf(rolProfesional)
        ))
        val perfil = perfilProfesionalRepository.save(PerfilProfesional(
            usuario             = u,
            especialidad        = t.especialidad,
            biografia           = t.biografia,
            urlAvatar           = t.urlAvatar,
            destacado           = t.destacado,
            localidad           = t.localidad,
            direccion           = t.direccion,
            precio              = t.precio,
            cobertura           = t.cobertura,
            matriculaNacional   = t.matriculaNacional,
            matriculaProvincial = t.matriculaProvincial,
            servicios           = t.servicios.toMutableList()
        ))
        val agenda = agendaRepository.save(Agenda(
            profesional = perfil,
            nombre      = t.agendaNombre,
            descripcion = t.agendaDescripcion
        ))
        val configs = listOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                             DayOfWeek.THURSDAY, DayOfWeek.FRIDAY).map { dia ->
            ConfiguracionHoraria(
                agenda              = agenda,
                diaSemana           = dia,
                inicioSlot          = LocalTime.of(9, 0),
                finSlot             = LocalTime.of(20, 0),
                duracionSlotMinutos = 45
            )
        }
        configuracionHorariaRepository.saveAll(configs)
        return ProfRecord(u, perfil, agenda)
    }
}
