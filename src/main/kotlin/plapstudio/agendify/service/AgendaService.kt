package plapstudio.agendify.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import plapstudio.agendify.domain.Agenda
import plapstudio.agendify.domain.ConfiguracionHoraria
import plapstudio.agendify.domain.EstadoTurno
import plapstudio.agendify.domain.ExcepcionAgenda
import plapstudio.agendify.dto.AgendaCreateRequest
import plapstudio.agendify.dto.AgendaUpdateRequest
import plapstudio.agendify.dto.ConfiguracionHorariaDto
import plapstudio.agendify.dto.ExcepcionAgendaDto
import plapstudio.agendify.dto.SlotDto
import plapstudio.agendify.errors.BusinessException
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.AgendaRepository
import plapstudio.agendify.repository.ConfiguracionHorariaRepository
import plapstudio.agendify.repository.PerfilProfesionalRepository
import plapstudio.agendify.repository.TurnoRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class AgendaService(
    private val agendaRepository:               AgendaRepository,
    private val perfilProfesionalRepository:    PerfilProfesionalRepository,
    private val configuracionHorariaRepository: ConfiguracionHorariaRepository,
    private val turnoRepository:                TurnoRepository
) {

    fun findById(id: UUID): Agenda =
        agendaRepository.findById(id).orElseThrow { NotFoundException("Agenda no encontrada con id: $id") }

    fun findAll(): List<Agenda> = agendaRepository.findAll()

    fun findActivas(): List<Agenda> = agendaRepository.findByActivaTrue()

    fun findByProfesional(profesionalId: Long): List<Agenda> {
        val perfil = perfilProfesionalRepository.findById(profesionalId)
            .orElseThrow { NotFoundException("Perfil profesional no encontrado con id: $profesionalId") }
        return agendaRepository.findByProfesional(perfil)
    }

    @Transactional
    fun create(req: AgendaCreateRequest): Agenda {
        val perfil = perfilProfesionalRepository.findById(req.profesionalId)
            .orElseThrow { NotFoundException("Perfil profesional no encontrado con id: ${req.profesionalId}") }
        if (!perfil.usuario.esProfesional()) {
            throw BusinessException("Solo un profesional puede crear una agenda")
        }
        val agenda = Agenda(
            profesional = perfil,
            nombre      = req.nombre,
            descripcion = req.descripcion
        )
        return agendaRepository.save(agenda)
    }

    @Transactional
    fun update(id: UUID, req: AgendaUpdateRequest): Agenda {
        val existente         = findById(id)
        existente.nombre      = req.nombre
        existente.descripcion = req.descripcion
        existente.activa      = req.activa
        return agendaRepository.save(existente)
    }

    @Transactional
    fun darDeBaja(id: UUID) {
        val agenda = findById(id)
        agenda.darDeBaja()
        agendaRepository.save(agenda)
    }

    // ── Configuración horaria ─────────────────────────────────────────────────

    @Transactional
    fun reemplazarConfiguraciones(agendaId: UUID, items: List<ConfiguracionHorariaDto>): Agenda {
        val agenda = findById(agendaId)
        agenda.configuraciones.clear()
        items.forEach {
            agenda.configuraciones.add(
                ConfiguracionHoraria(
                    agenda              = agenda,
                    diaSemana           = it.diaSemana,
                    inicioSlot          = it.inicioSlot,
                    finSlot             = it.finSlot,
                    duracionSlotMinutos = it.duracionSlotMinutos
                )
            )
        }
        return agendaRepository.save(agenda)
    }

    @Transactional
    fun agregarConfiguracion(agendaId: UUID, dto: ConfiguracionHorariaDto): Agenda {
        val agenda = findById(agendaId)
        agenda.configuraciones.add(
            ConfiguracionHoraria(
                agenda              = agenda,
                diaSemana           = dto.diaSemana,
                inicioSlot          = dto.inicioSlot,
                finSlot             = dto.finSlot,
                duracionSlotMinutos = dto.duracionSlotMinutos
            )
        )
        return agendaRepository.save(agenda)
    }

    @Transactional
    fun eliminarConfiguracion(agendaId: UUID, configId: UUID): Agenda {
        val agenda = findById(agendaId)
        agenda.configuraciones.removeIf { it.id == configId }
        return agendaRepository.save(agenda)
    }

    // ── Excepciones ───────────────────────────────────────────────────────────

    @Transactional
    fun agregarExcepcion(agendaId: UUID, dto: ExcepcionAgendaDto): Agenda {
        val agenda = findById(agendaId)
        agenda.excepciones.add(
            ExcepcionAgenda(
                agenda      = agenda,
                fechaInicio = dto.fechaInicio,
                fechaFin    = dto.fechaFin,
                motivo      = dto.motivo
            )
        )
        return agendaRepository.save(agenda)
    }

    @Transactional
    fun eliminarExcepcion(agendaId: UUID, excepcionId: UUID): Agenda {
        val agenda = findById(agendaId)
        agenda.excepciones.removeIf { it.id == excepcionId }
        return agendaRepository.save(agenda)
    }

    // ── Slots disponibles ─────────────────────────────────────────────────────

    fun slotsDisponibles(agendaId: UUID, fecha: LocalDate): List<SlotDto> {
        val agenda = findById(agendaId)
        if (!agenda.activa) return emptyList()
        if (agenda.tieneExcepcionEn(fecha)) return emptyList()

        val configs = agenda.configuraciones.filter { it.diaSemana == fecha.dayOfWeek }
        if (configs.isEmpty()) return emptyList()

        val turnos = turnoRepository.findByAgenda(agenda)
            .filter { it.estado != EstadoTurno.CANCELADO }
            .filter { it.iniciaEn.toLocalDate() == fecha }

        val slots = mutableListOf<SlotDto>()
        configs.forEach { c ->
            var actual = LocalDateTime.of(fecha, c.inicioSlot)
            val limite = LocalDateTime.of(fecha, c.finSlot)
            while (!actual.plusMinutes(c.duracionSlotMinutos.toLong()).isAfter(limite)) {
                val finNuevo = actual.plusMinutes(c.duracionSlotMinutos.toLong())
                val ocupado = turnos.any { t ->
                    val finExistente = t.iniciaEn.plusMinutes(t.duracionMinutos.toLong())
                    actual.isBefore(finExistente) && t.iniciaEn.isBefore(finNuevo)
                }
                slots.add(SlotDto(
                    iniciaEn        = actual,
                    duracionMinutos = c.duracionSlotMinutos,
                    disponible      = !ocupado
                ))
                actual = finNuevo
            }
        }
        return slots.sortedBy { it.iniciaEn }
    }
}
