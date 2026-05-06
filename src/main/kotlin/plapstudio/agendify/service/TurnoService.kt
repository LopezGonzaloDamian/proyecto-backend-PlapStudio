package plapstudio.agendify.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import plapstudio.agendify.domain.*
import plapstudio.agendify.dto.TurnoCreateRequest
import plapstudio.agendify.dto.TurnoUpdateRequest
import plapstudio.agendify.errors.BusinessException
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class TurnoService(
    private val turnoRepository:             TurnoRepository,
    private val historialTurnoRepository:    HistorialTurnoRepository,
    private val agendaService:               AgendaService,
    private val perfilClienteRepository:     PerfilClienteRepository,
    private val perfilProfesionalRepository: PerfilProfesionalRepository,
    private val pagoRepository:              PagoRepository,
    private val notificacionRepository:      NotificacionRepository
) {

    fun findById(id: UUID): Turno =
        turnoRepository.findById(id).orElseThrow { NotFoundException("Turno no encontrado con id: $id") }

    fun pagoDe(turno: Turno): Pago? = pagoRepository.findByTurno(turno)

    fun findAll(): List<Turno> = turnoRepository.findAll()

    fun findByAgenda(agendaId: UUID): List<Turno> {
        val agenda = agendaService.findById(agendaId)
        return turnoRepository.findByAgenda(agenda)
    }

    fun findByCliente(clienteId: Long): List<Turno> {
        val perfil = perfilClienteRepository.findById(clienteId)
            .orElseThrow { NotFoundException("Perfil cliente no encontrado con id: $clienteId") }
        return turnoRepository.findByCliente(perfil)
    }

    fun findByProfesional(profesionalId: Long): List<Turno> {
        val perfil = perfilProfesionalRepository.findById(profesionalId)
            .orElseThrow { NotFoundException("Perfil profesional no encontrado con id: $profesionalId") }
        return turnoRepository.findByAgendaProfesional(perfil)
    }

    @Transactional
    fun reservar(req: TurnoCreateRequest): Turno {
        val agenda  = agendaService.findById(req.agendaId)
        val cliente = perfilClienteRepository.findById(req.clienteId)
            .orElseThrow { NotFoundException("Perfil cliente no encontrado con id: ${req.clienteId}") }
        if (!cliente.usuario.esCliente()) {
            throw BusinessException("Solo un cliente puede reservar un turno")
        }
        val fecha = req.iniciaEn.toLocalDate()
        val hora  = req.iniciaEn.toLocalTime()
        if (agenda.tieneExcepcionEn(fecha)) {
            throw BusinessException("La agenda no está disponible en esa fecha")
        }
        if (!agenda.estaDisponibleEn(req.iniciaEn.dayOfWeek, hora, req.duracionMinutos)) {
            throw BusinessException("La agenda no está disponible en el horario solicitado")
        }
        if (turnoRepository.existsByAgendaAndIniciaEn(agenda, req.iniciaEn)) {
            throw BusinessException("Ya existe un turno en esa fecha y horario")
        }
        val turno = turnoRepository.save(Turno(
            agenda          = agenda,
            cliente         = cliente,
            iniciaEn        = req.iniciaEn,
            duracionMinutos = req.duracionMinutos,
            notas           = req.notas,
            estado          = EstadoTurno.PENDIENTE
        ))
        if (req.pagarAlReservar) {
            pagoRepository.save(Pago(
                turno                   = turno,
                monto                   = agenda.profesional.precio,
                estado                  = EstadoPago.APROBADO,
                referenciaProveedorMock = "MOCK-${req.medioPago ?: "PAGO"}-${turno.id}",
                pagadoEn                = LocalDateTime.now()
            ))
        } else if (agenda.profesional.precio > BigDecimal.ZERO) {
            pagoRepository.save(Pago(
                turno  = turno,
                monto  = agenda.profesional.precio,
                estado = EstadoPago.PENDIENTE
            ))
        }
        notificacionRepository.save(Notificacion(
            usuario     = agenda.profesional.usuario,
            canal       = "IN_APP",
            titulo      = "Nuevo turno reservado",
            cuerpo      = "${cliente.usuario.nombreCompleto} reservó un turno para ${req.iniciaEn}.",
            recursoTipo = "TURNO",
            recursoId   = turno.id
        ))
        notificacionRepository.save(Notificacion(
            usuario     = cliente.usuario,
            canal       = "IN_APP",
            titulo      = "Turno registrado",
            cuerpo      = "Tu turno con ${agenda.profesional.usuario.nombreCompleto} quedó registrado.",
            recursoTipo = "TURNO",
            recursoId   = turno.id
        ))
        return turno
    }

    @Transactional
    fun modificar(id: UUID, req: TurnoUpdateRequest): Turno {
        val turno          = findById(id)
        val estadoAnterior = turno.estado
        val nuevoEstado    = runCatching { EstadoTurno.valueOf(req.estado.uppercase()) }
            .getOrElse { throw BusinessException("Estado inválido: ${req.estado}") }

        if (req.iniciaEn != turno.iniciaEn || req.duracionMinutos != turno.duracionMinutos) {
            if (turno.agenda.tieneExcepcionEn(req.iniciaEn.toLocalDate())) {
                throw BusinessException("La agenda no está disponible en esa fecha")
            }
            if (!turno.agenda.estaDisponibleEn(req.iniciaEn.dayOfWeek, req.iniciaEn.toLocalTime(), req.duracionMinutos)) {
                throw BusinessException("La agenda no está disponible en el horario solicitado")
            }
            val choque = turnoRepository.findByAgenda(turno.agenda)
                .any { it.id != turno.id && it.estado != EstadoTurno.CANCELADO && it.iniciaEn == req.iniciaEn }
            if (choque) throw BusinessException("Ya existe un turno en esa fecha y horario")
        }

        turno.iniciaEn        = req.iniciaEn
        turno.duracionMinutos = req.duracionMinutos
        turno.notas           = req.notas
        turno.estado          = nuevoEstado
        turno.actualizadoEn   = LocalDateTime.now()
        val saved = turnoRepository.save(turno)
        if (estadoAnterior != saved.estado) {
            historialTurnoRepository.save(HistorialTurno(
                turno          = saved,
                estadoAnterior = estadoAnterior,
                estadoNuevo    = saved.estado
            ))
        }
        return saved
    }

    @Transactional
    fun actualizarNotas(id: UUID, notas: String): Turno {
        val turno           = findById(id)
        turno.notas         = notas
        turno.actualizadoEn = LocalDateTime.now()
        return turnoRepository.save(turno)
    }

    @Transactional
    fun cancelar(id: UUID, motivo: String? = null): Turno {
        val turno          = findById(id)
        val estadoAnterior = turno.estado
        turno.cancelar()
        if (!motivo.isNullOrBlank()) turno.notas = motivo
        val saved = turnoRepository.save(turno)
        registrarCambio(saved, estadoAnterior)
        return saved
    }

    @Transactional
    fun confirmar(id: UUID): Turno {
        val turno          = findById(id)
        val estadoAnterior = turno.estado
        turno.confirmar()
        val saved = turnoRepository.save(turno)
        registrarCambio(saved, estadoAnterior)
        return saved
    }

    @Transactional
    fun completar(id: UUID): Turno {
        val turno          = findById(id)
        val estadoAnterior = turno.estado
        turno.completar()
        val saved = turnoRepository.save(turno)
        registrarCambio(saved, estadoAnterior)
        return saved
    }

    private fun registrarCambio(turno: Turno, estadoAnterior: EstadoTurno) {
        historialTurnoRepository.save(HistorialTurno(
            turno          = turno,
            estadoAnterior = estadoAnterior,
            estadoNuevo    = turno.estado
        ))
        notificacionRepository.save(Notificacion(
            usuario     = turno.cliente.usuario,
            canal       = "IN_APP",
            titulo      = "Turno ${turno.estado.name.lowercase()}",
            cuerpo      = "Tu turno con ${turno.agenda.profesional.usuario.nombreCompleto} quedó ${turno.estado.name.lowercase()}.",
            recursoTipo = "TURNO",
            recursoId   = turno.id
        ))
    }
}
