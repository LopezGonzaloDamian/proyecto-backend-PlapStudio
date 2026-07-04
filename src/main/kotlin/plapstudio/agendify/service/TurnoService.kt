package plapstudio.agendify.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import plapstudio.agendify.domain.EstadoPago
import plapstudio.agendify.domain.EstadoTurno
import plapstudio.agendify.domain.HistorialTurno
import plapstudio.agendify.domain.Notificacion
import plapstudio.agendify.domain.OrigenPago
import plapstudio.agendify.domain.Pago
import plapstudio.agendify.domain.PerfilProfesional
import plapstudio.agendify.domain.Turno
import plapstudio.agendify.dto.TurnoCreateRequest
import plapstudio.agendify.dto.TurnoUpdateRequest
import plapstudio.agendify.errors.BusinessException
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.HistorialTurnoRepository
import plapstudio.agendify.repository.NotificacionRepository
import plapstudio.agendify.repository.PagoRepository
import plapstudio.agendify.repository.PerfilClienteRepository
import plapstudio.agendify.repository.PerfilProfesionalRepository
import plapstudio.agendify.repository.TurnoRepository
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@Service
class TurnoService(
    private val turnoRepository: TurnoRepository,
    private val historialTurnoRepository: HistorialTurnoRepository,
    private val agendaService: AgendaService,
    private val perfilClienteRepository: PerfilClienteRepository,
    private val perfilProfesionalRepository: PerfilProfesionalRepository,
    private val pagoRepository: PagoRepository,
    private val notificacionRepository: NotificacionRepository,
    private val n8nWebhookService: N8nWebhookService
) {
    private val zonaHorariaApp = ZoneId.of("America/Argentina/Buenos_Aires")
    private val comisionBasePorcentaje = BigDecimal("5.00")
    private val cien = BigDecimal("100")
    private val formatoHoraWebhook = DateTimeFormatter.ofPattern("HH:mm")

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
        val agenda = agendaService.findById(req.agendaId)
        if (!agenda.activa) {
            throw BusinessException("La agenda no esta activa")
        }

        val cliente = req.clienteId?.let { clienteId ->
            perfilClienteRepository.findById(clienteId)
                .orElseThrow { NotFoundException("Perfil cliente no encontrado con id: $clienteId") }
        }

        if (cliente != null && !cliente.usuario.esCliente()) {
            throw BusinessException("Solo un cliente puede reservar un turno")
        }
        if (cliente == null && (req.clienteExternoNombre.isNullOrBlank() || req.clienteExternoTelefono.isNullOrBlank())) {
            throw BusinessException("Para un cliente no registrado se requiere nombre y telefono")
        }
        if (req.duracionMinutos <= 0) {
            throw BusinessException("La duracion del turno debe ser mayor a cero")
        }

        val fecha = req.iniciaEn.toLocalDate()
        val hora = req.iniciaEn.toLocalTime()
        if (!req.iniciaEn.isAfter(LocalDateTime.now(zonaHorariaApp))) {
            throw BusinessException("No se puede reservar un turno en un horario que ya paso")
        }
        if (agenda.tieneExcepcionEn(fecha)) {
            throw BusinessException("La agenda no esta disponible en esa fecha")
        }
        if (!agenda.estaDisponibleEn(req.iniciaEn.dayOfWeek, hora, req.duracionMinutos)) {
            throw BusinessException("La agenda no esta disponible en el horario solicitado")
        }
        if (turnoRepository.existsByAgendaAndIniciaEn(agenda, req.iniciaEn)) {
            throw BusinessException("Ya existe un turno en esa fecha y horario")
        }

        val precioTurno = precioServicioSolicitado(agenda.profesional, req.notas)
            ?: req.precioServicio?.takeIf { it > BigDecimal.ZERO }
            ?: agenda.profesional.precio

        val turno = turnoRepository.save(
            Turno(
                agenda = agenda,
                cliente = cliente,
                clienteExternoNombre = req.clienteExternoNombre?.trim()?.takeIf { it.isNotBlank() },
                clienteExternoTelefono = req.clienteExternoTelefono?.trim()?.takeIf { it.isNotBlank() },
                clienteExternoDni = req.clienteExternoDni?.trim()?.takeIf { it.isNotBlank() },
                clienteExternoEmail = req.clienteExternoEmail?.trim()?.takeIf { it.isNotBlank() },
                iniciaEn = req.iniciaEn,
                duracionMinutos = req.duracionMinutos,
                precio = precioTurno,
                notas = req.notas.trim(),
                estado = EstadoTurno.CONFIRMADO
            )
        )

        if (req.pagarAlReservar) {
            val senaReserva = precioTurno
                .multiply(BigDecimal("0.5"))
                .setScale(0, RoundingMode.HALF_UP)
                .max(BigDecimal("500"))
            val porcentajeComision = comisionBasePorcentaje.setScale(2, RoundingMode.HALF_UP)
            val montoComision = calcularComision(senaReserva, porcentajeComision)
            pagoRepository.save(
                Pago(
                    turno = turno,
                    monto = senaReserva,
                    porcentajeComision = porcentajeComision,
                    montoComision = montoComision,
                    estado = EstadoPago.APROBADO,
                    origen = OrigenPago.ONLINE,
                    referenciaProveedorMock = "MOCK-${req.medioPago ?: "PAGO"}-${turno.id}",
                    pagadoEn = LocalDateTime.now()
                )
            )
        } else if (precioTurno > BigDecimal.ZERO) {
            pagoRepository.save(
                Pago(
                    turno = turno,
                    monto = precioTurno,
                    porcentajeComision = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    montoComision = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                    estado = EstadoPago.APROBADO,
                    origen = OrigenPago.EXTERNO,
                    pagadoEn = LocalDateTime.now()
                )
            )
        }

        notificacionRepository.save(
            Notificacion(
                usuario = agenda.profesional.usuario,
                canal = "IN_APP",
                titulo = "Nuevo turno reservado",
                cuerpo = "${nombreCliente(turno)} reservo un turno para ${req.iniciaEn}.",
                recursoTipo = "TURNO",
                recursoId = turno.id
            )
        )
        cliente?.let {
            notificacionRepository.save(
                Notificacion(
                    usuario = it.usuario,
                    canal = "IN_APP",
                    titulo = "Turno registrado",
                    cuerpo = "Tu turno con ${agenda.profesional.usuario.nombreCompleto} quedo registrado.",
                    recursoTipo = "TURNO",
                    recursoId = turno.id
                )
            )
        }
        notificarTurnoConfirmadoLuegoDelCommit(turno)
        return turno
    }

    @Transactional
    fun modificar(id: UUID, req: TurnoUpdateRequest): Turno {
        val turno = findById(id)
        val estadoAnterior = turno.estado
        val nuevoEstado = runCatching { EstadoTurno.valueOf(req.estado.uppercase()) }
            .getOrElse { throw BusinessException("Estado invalido: ${req.estado}") }

        if (req.duracionMinutos <= 0) {
            throw BusinessException("La duracion del turno debe ser mayor a cero")
        }

        if (req.iniciaEn != turno.iniciaEn || req.duracionMinutos != turno.duracionMinutos) {
            if (!req.iniciaEn.isAfter(LocalDateTime.now(zonaHorariaApp))) {
                throw BusinessException("No se puede mover un turno a un horario que ya paso")
            }
            if (turno.agenda.tieneExcepcionEn(req.iniciaEn.toLocalDate())) {
                throw BusinessException("La agenda no esta disponible en esa fecha")
            }
            if (!turno.agenda.estaDisponibleEn(req.iniciaEn.dayOfWeek, req.iniciaEn.toLocalTime(), req.duracionMinutos)) {
                throw BusinessException("La agenda no esta disponible en el horario solicitado")
            }
            val choque = turnoRepository.findByAgenda(turno.agenda)
                .any { existente ->
                    existente.id != turno.id &&
                        existente.estado != EstadoTurno.CANCELADO &&
                        existente.iniciaEn == req.iniciaEn
                }
            if (choque) {
                throw BusinessException("Ya existe un turno en esa fecha y horario")
            }
        }

        turno.iniciaEn = req.iniciaEn
        turno.duracionMinutos = req.duracionMinutos
        turno.notas = req.notas.trim()
        turno.estado = nuevoEstado
        turno.actualizadoEn = LocalDateTime.now()
        val saved = turnoRepository.save(turno)
        if (estadoAnterior != saved.estado) {
            historialTurnoRepository.save(
                HistorialTurno(
                    turno = saved,
                    estadoAnterior = estadoAnterior,
                    estadoNuevo = saved.estado
                )
            )
        }
        return saved
    }

    @Transactional
    fun actualizarNotas(id: UUID, notas: String): Turno {
        val turno = findById(id)
        turno.notas = notas.trim()
        turno.actualizadoEn = LocalDateTime.now()
        return turnoRepository.save(turno)
    }

    @Transactional
    fun cancelar(id: UUID, motivo: String? = null): Turno {
        val turno = findById(id)
        val estadoAnterior = turno.estado
        turno.cancelar()
        if (!motivo.isNullOrBlank()) {
            turno.notas = motivo.trim()
        }
        val saved = turnoRepository.save(turno)
        registrarCambio(saved, estadoAnterior)
        return saved
    }

    private fun registrarCambio(turno: Turno, estadoAnterior: EstadoTurno) {
        historialTurnoRepository.save(
            HistorialTurno(
                turno = turno,
                estadoAnterior = estadoAnterior,
                estadoNuevo = turno.estado
            )
        )
        turno.cliente?.let { cliente ->
            notificacionRepository.save(
                Notificacion(
                    usuario = cliente.usuario,
                    canal = "IN_APP",
                    titulo = "Turno ${turno.estado.name.lowercase()}",
                    cuerpo = "Tu turno con ${turno.agenda.profesional.usuario.nombreCompleto} quedo ${turno.estado.name.lowercase()}.",
                    recursoTipo = "TURNO",
                    recursoId = turno.id
                )
            )
        }
    }

    private fun nombreCliente(turno: Turno): String =
        turno.cliente?.usuario?.nombreCompleto ?: turno.clienteExternoNombre ?: "Cliente externo"

    private fun notificarTurnoConfirmadoLuegoDelCommit(turno: Turno) {
        val payload = TurnoConfirmadoWebhookPayload(
            nombre = nombreCliente(turno),
            mail = mailCliente(turno),
            profesional = turno.agenda.profesional.usuario.nombreCompleto,
            especialidad = turno.agenda.profesional.especialidad,
            fechaTurno = turno.iniciaEn.toLocalDate().toString(),
            horaTurno = turno.iniciaEn.toLocalTime().format(formatoHoraWebhook)
        )

        ejecutarLuegoDelCommit {
            n8nWebhookService.notificarTurnoConfirmado(payload)
        }
    }

    private fun mailCliente(turno: Turno): String? =
        turno.cliente?.usuario?.email ?: turno.clienteExternoEmail

    private fun ejecutarLuegoDelCommit(action: () -> Unit) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            action()
            return
        }

        TransactionSynchronizationManager.registerSynchronization(
            object : TransactionSynchronization {
                override fun afterCommit() {
                    action()
                }
            }
        )
    }

    private fun calcularComision(monto: BigDecimal, porcentaje: BigDecimal): BigDecimal =
        monto.multiply(porcentaje)
            .divide(cien, 2, RoundingMode.HALF_UP)
            .setScale(0, RoundingMode.HALF_UP)

    private fun precioServicioSolicitado(profesional: PerfilProfesional, notas: String): BigDecimal? =
        profesional.serviciosConPrecio
            .firstOrNull { notas.contains(it.nombre, ignoreCase = true) }
            ?.precio
}
