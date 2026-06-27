package plapstudio.agendify

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import plapstudio.agendify.auth.AuthTokenService
import plapstudio.agendify.auth.PasswordService
import plapstudio.agendify.domain.Agenda
import plapstudio.agendify.domain.ConfiguracionHoraria
import plapstudio.agendify.domain.EstadoAsignacionAsistente
import plapstudio.agendify.domain.PerfilCliente
import plapstudio.agendify.domain.PerfilProfesional
import plapstudio.agendify.domain.ProfesionalAsistente
import plapstudio.agendify.domain.Rol
import plapstudio.agendify.domain.Usuario
import plapstudio.agendify.repository.AgendaRepository
import plapstudio.agendify.repository.ConfiguracionHorariaRepository
import plapstudio.agendify.repository.PerfilClienteRepository
import plapstudio.agendify.repository.PerfilProfesionalRepository
import plapstudio.agendify.repository.ProfesionalAsistenteRepository
import plapstudio.agendify.repository.RolRepository
import plapstudio.agendify.repository.TurnoRepository
import plapstudio.agendify.repository.UsuarioRepository
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.LocalTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class IntegrationTestSupport {

    @Autowired protected lateinit var mockMvc: MockMvc
    @Autowired protected lateinit var objectMapper: ObjectMapper
    @Autowired protected lateinit var authTokenService: AuthTokenService
    @Autowired protected lateinit var passwordService: PasswordService
    @Autowired protected lateinit var rolRepository: RolRepository
    @Autowired protected lateinit var usuarioRepository: UsuarioRepository
    @Autowired protected lateinit var perfilClienteRepository: PerfilClienteRepository
    @Autowired protected lateinit var perfilProfesionalRepository: PerfilProfesionalRepository
    @Autowired protected lateinit var agendaRepository: AgendaRepository
    @Autowired protected lateinit var configuracionHorariaRepository: ConfiguracionHorariaRepository
    @Autowired protected lateinit var turnoRepository: TurnoRepository
    @Autowired protected lateinit var profesionalAsistenteRepository: ProfesionalAsistenteRepository

    protected fun json(body: Any): String = objectMapper.writeValueAsString(body)

    protected fun authHeader(usuario: Usuario): String = "Bearer ${authTokenService.issueToken(usuario.id!!)}"

    protected fun role(nombre: String): Rol =
        rolRepository.findByNombre(nombre) ?: rolRepository.save(Rol(nombre = nombre, descripcion = nombre))

    protected fun cliente(
        email: String = "cliente@example.com",
        password: String = "1234",
        nombre: String = "Cliente Test"
    ): Pair<Usuario, PerfilCliente> {
        val usuario = usuarioRepository.save(
            Usuario(
                email = email,
                contrasenaHash = passwordService.hash(password),
                nombreCompleto = nombre,
                telefono = "111111111",
                roles = mutableSetOf(role("CLIENTE"))
            )
        )
        val perfil = perfilClienteRepository.save(PerfilCliente(usuario = usuario))
        usuario.perfilCliente = perfil
        return usuarioRepository.save(usuario) to perfil
    }

    protected fun profesional(
        email: String = "pro@example.com",
        password: String = "1234",
        nombre: String = "Profesional Test"
    ): Pair<Usuario, PerfilProfesional> {
        val usuario = usuarioRepository.save(
            Usuario(
                email = email,
                contrasenaHash = passwordService.hash(password),
                nombreCompleto = nombre,
                telefono = "222222222",
                roles = mutableSetOf(role("PROFESIONAL"))
            )
        )
        val perfil = perfilProfesionalRepository.save(
            PerfilProfesional(
                usuario = usuario,
                especialidad = "Nutricion",
                biografia = "Bio",
                localidad = "San Martin",
                direccion = "Calle 123",
                precio = BigDecimal("25000"),
                servicios = mutableListOf("Consulta")
            )
        )
        usuario.perfilProfesional = perfil
        return usuarioRepository.save(usuario) to perfil
    }

    protected fun asistente(
        email: String = "asistente@example.com",
        password: String = "1234",
        nombre: String = "Asistente Test"
    ): Usuario =
        usuarioRepository.save(
            Usuario(
                email = email,
                contrasenaHash = passwordService.hash(password),
                nombreCompleto = nombre,
                telefono = "333333333",
                roles = mutableSetOf(role("ASISTENTE"))
            )
        )

    protected fun agenda(
        profesional: PerfilProfesional,
        nombre: String = "Agenda principal",
        descripcion: String = "Agenda de prueba"
    ): Agenda =
        agendaRepository.save(
            Agenda(
                profesional = profesional,
                nombre = nombre,
                descripcion = descripcion
            )
        )

    protected fun configurarAgenda(
        agenda: Agenda,
        diaSemana: DayOfWeek = DayOfWeek.MONDAY,
        inicio: LocalTime = LocalTime.of(9, 0),
        fin: LocalTime = LocalTime.of(18, 0),
        duracion: Int = 30
    ) {
        val configuracion = configuracionHorariaRepository.save(
            ConfiguracionHoraria(
                agenda = agenda,
                diaSemana = diaSemana,
                inicioSlot = inicio,
                finSlot = fin,
                duracionSlotMinutos = duracion
            )
        )
        agenda.configuraciones.add(configuracion)
    }

    protected fun vincularAsistente(profesional: PerfilProfesional, asistente: Usuario) {
        profesionalAsistenteRepository.save(
            ProfesionalAsistente(
                profesional = profesional,
                asistente = asistente,
                estado = EstadoAsignacionAsistente.ACEPTADA
            )
        )
    }

    protected val jsonUtf8: MediaType = MediaType.APPLICATION_JSON
}
