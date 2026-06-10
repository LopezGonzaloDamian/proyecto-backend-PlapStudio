package plapstudio.agendify.dto

import org.springframework.stereotype.Component
import plapstudio.agendify.domain.*
import java.math.BigDecimal

@Component
class Mapper {

    fun toUsuarioDto(usuario: Usuario): UsuarioDto = UsuarioDto(
        id                  = usuario.id!!,
        email               = usuario.email,
        nombreCompleto      = usuario.nombreCompleto,
        telefono            = usuario.telefono,
        urlAvatar           = usuario.urlAvatar,
        activo              = usuario.activo,
        roles               = usuario.roles.map { it.nombre },
        perfilProfesionalId = usuario.perfilProfesional?.id,
        perfilClienteId     = usuario.perfilCliente?.id,
        requiereSeleccionRol = usuario.requiereSeleccionRol()
    )

    fun toProfesionalDto(perfil: PerfilProfesional, agendas: List<Agenda>): ProfesionalDto = ProfesionalDto(
        id                  = perfil.id!!,
        nombreCompleto      = perfil.usuario.nombreCompleto,
        email               = perfil.usuario.email,
        telefono            = perfil.usuario.telefono,
        especialidad        = perfil.especialidad,
        biografia           = perfil.biografia,
        urlAvatar           = perfil.urlAvatar,
        destacado           = perfil.destacado,
        localidad           = perfil.localidad,
        direccion           = perfil.direccion,
        precio              = perfil.precio,
        comisionPendientePorcentaje = perfil.comisionPendientePorcentaje ?: BigDecimal.ZERO,
        cobertura           = perfil.cobertura,
        matriculaNacional   = perfil.matriculaNacional,
        matriculaProvincial = perfil.matriculaProvincial,
        servicios           = perfil.servicios.toList(),
        agendas             = agendas.map { toAgendaResumenDto(it) }
    )

    fun toProfesionalSummaryDto(perfil: PerfilProfesional): ProfesionalSummaryDto = ProfesionalSummaryDto(
        id             = perfil.id!!,
        nombreCompleto = perfil.usuario.nombreCompleto,
        especialidad   = perfil.especialidad,
        urlAvatar      = perfil.urlAvatar,
        localidad      = perfil.localidad,
        precio         = perfil.precio,
        destacado      = perfil.destacado,
        servicios      = perfil.servicios.toList()
    )

    fun toResenaDto(resena: ResenaProfesional): ResenaDto {
        val nombre = resena.cliente.usuario.nombreCompleto
        val iniciales = nombre
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "CL" }
        return ResenaDto(
            id               = resena.id!!,
            profesionalId    = resena.profesional.id!!,
            clienteId        = resena.cliente.id!!,
            clienteNombre    = nombre,
            clienteIniciales = iniciales,
            turnoId          = resena.turno.id!!,
            calificacion     = resena.calificacion,
            comentario       = resena.comentario,
            creadaEn         = resena.creadaEn
        )
    }

    fun toClienteDto(perfil: PerfilCliente): ClienteDto = ClienteDto(
        id             = perfil.id!!,
        nombreCompleto = perfil.usuario.nombreCompleto,
        email          = perfil.usuario.email,
        telefono       = perfil.usuario.telefono,
        notas          = perfil.notas
    )

    fun toAgendaResumenDto(agenda: Agenda): AgendaResumenDto = AgendaResumenDto(
        id          = agenda.id!!,
        nombre      = agenda.nombre,
        descripcion = agenda.descripcion,
        activa      = agenda.activa
    )

    fun toAgendaDto(agenda: Agenda): AgendaDto = AgendaDto(
        id                = agenda.id!!,
        nombre            = agenda.nombre,
        descripcion       = agenda.descripcion,
        activa            = agenda.activa,
        profesionalId     = agenda.profesional.id!!,
        profesionalNombre = agenda.profesional.usuario.nombreCompleto,
        configuraciones   = agenda.configuraciones.map { toConfiguracionDto(it) },
        excepciones       = agenda.excepciones.map { toExcepcionDto(it) }
    )

    fun toConfiguracionDto(c: ConfiguracionHoraria): ConfiguracionHorariaDto = ConfiguracionHorariaDto(
        id                  = c.id,
        diaSemana           = c.diaSemana,
        inicioSlot          = c.inicioSlot,
        finSlot             = c.finSlot,
        duracionSlotMinutos = c.duracionSlotMinutos
    )

    fun toExcepcionDto(e: ExcepcionAgenda): ExcepcionAgendaDto = ExcepcionAgendaDto(
        id          = e.id,
        fechaInicio = e.fechaInicio,
        fechaFin    = e.fechaFin,
        motivo      = e.motivo
    )

    fun toTurnoDto(turno: Turno, pago: Pago? = null): TurnoDto = TurnoDto(
        id                = turno.id!!,
        agendaId          = turno.agenda.id!!,
        agendaNombre      = turno.agenda.nombre,
        profesionalId     = turno.agenda.profesional.id!!,
        profesionalNombre = turno.agenda.profesional.usuario.nombreCompleto,
        clienteId         = turno.cliente?.id,
        clienteNombre     = turno.cliente?.usuario?.nombreCompleto ?: turno.clienteExternoNombre ?: "Cliente externo",
        clienteTelefono   = turno.cliente?.usuario?.telefono ?: turno.clienteExternoTelefono,
        clienteDni        = turno.clienteExternoDni,
        clienteEmail      = turno.cliente?.usuario?.email ?: turno.clienteExternoEmail,
        iniciaEn          = turno.iniciaEn,
        duracionMinutos   = turno.duracionMinutos,
        estado            = turno.estado.name,
        notas             = turno.notas,
        pago              = pago?.let { toPagoDto(it) }
    )

    fun toPagoDto(pago: Pago): PagoDto = PagoDto(
        id                      = pago.id!!,
        turnoId                 = pago.turno.id!!,
        monto                   = pago.monto,
        moneda                  = pago.moneda,
        porcentajeComision      = pago.porcentajeComision ?: BigDecimal.ZERO,
        montoComision           = pago.montoComision ?: BigDecimal.ZERO,
        estado                  = pago.estado.name,
        origen                  = pago.origen?.name ?: OrigenPago.EXTERNO.name,
        referenciaProveedorMock = pago.referenciaProveedorMock,
        pagadoEn                = pago.pagadoEn
    )

    fun toFacturaDto(f: Factura): FacturaDto = FacturaDto(
        id                = f.id!!,
        pagoId            = f.pago.id!!,
        clienteId         = f.cliente.id!!,
        clienteNombre     = f.cliente.usuario.nombreCompleto,
        profesionalId     = f.profesional.id!!,
        profesionalNombre = f.profesional.usuario.nombreCompleto,
        numeroFactura     = f.numeroFactura,
        montoTotal        = f.montoTotal,
        moneda            = f.moneda,
        estado            = f.estado.name,
        emitidaEn         = f.emitidaEn
    )

    fun toFavoritoDto(f: Favorito): FavoritoDto = FavoritoDto(
        id          = f.id!!,
        clienteId   = f.cliente.id!!,
        profesional = toProfesionalSummaryDto(f.profesional),
        agregadoEn  = f.agregadoEn
    )

    fun toNotificacionDto(n: Notificacion): NotificacionDto = NotificacionDto(
        id          = n.id!!,
        usuarioId   = n.usuario.id!!,
        canal       = n.canal,
        titulo      = n.titulo,
        cuerpo      = n.cuerpo,
        leida       = n.leida,
        enviadaEn   = n.enviadaEn,
        recursoTipo = n.recursoTipo,
        recursoId   = n.recursoId
    )

    fun toAsistenteAsignacionDto(a: ProfesionalAsistente): AsistenteAsignacionDto = AsistenteAsignacionDto(
        id                       = a.id!!,
        profesionalId            = a.profesional.id!!,
        profesionalNombre        = a.profesional.usuario.nombreCompleto,
        profesionalEspecialidad  = a.profesional.especialidad,
        profesionalAvatarUrl     = a.profesional.urlAvatar,
        asistenteId              = a.asistente.id!!,
        asistenteNombre          = a.asistente.nombreCompleto,
        asistenteEmail           = a.asistente.email,
        estado                   = a.estado.name,
        asignadoEn               = a.asignadoEn
    )
}
