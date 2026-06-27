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
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Service
class AgendaService(
    private val agendaRepository: AgendaRepository,
    private val perfilProfesionalRepository: PerfilProfesionalRepository,
    private val configuracionHorariaRepository: ConfiguracionHorariaRepository,
    private val turnoRepository: TurnoRepository
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
        validarAgenda(req.nombre, req.descripcion)
        return agendaRepository.save(
            Agenda(
                profesional = perfil,
                nombre = req.nombre.trim(),
                descripcion = req.descripcion.trim()
            )
        )
    }

    @Transactional
    fun update(id: UUID, req: AgendaUpdateRequest): Agenda {
        validarAgenda(req.nombre, req.descripcion)
        val existente = findById(id)
        existente.nombre = req.nombre.trim()
        existente.descripcion = req.descripcion.trim()
        existente.activa = req.activa
        return agendaRepository.save(existente)
    }

    @Transactional
    fun darDeBaja(id: UUID) {
        val agenda = findById(id)
        agenda.darDeBaja()
        agendaRepository.save(agenda)
    }

    @Transactional
    fun reemplazarConfiguraciones(agendaId: UUID, items: List<ConfiguracionHorariaDto>): Agenda {
        val agenda = findById(agendaId)
        agenda.configuraciones.clear()
        items.forEach { dto ->
            validarConfiguracion(dto)
            agenda.configuraciones.add(
                ConfiguracionHoraria(
                    agenda = agenda,
                    diaSemana = dto.diaSemana,
                    inicioSlot = dto.inicioSlot,
                    finSlot = dto.finSlot,
                    duracionSlotMinutos = dto.duracionSlotMinutos
                )
            )
        }
        return agendaRepository.save(agenda)
    }

    @Transactional
    fun agregarConfiguracion(agendaId: UUID, dto: ConfiguracionHorariaDto): Agenda {
        val agenda = findById(agendaId)
        validarConfiguracion(dto)
        agenda.configuraciones.add(
            ConfiguracionHoraria(
                agenda = agenda,
                diaSemana = dto.diaSemana,
                inicioSlot = dto.inicioSlot,
                finSlot = dto.finSlot,
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

    @Transactional
    fun agregarExcepcion(agendaId: UUID, dto: ExcepcionAgendaDto): Agenda {
        val agenda = findById(agendaId)
        validarExcepcion(dto)
        agenda.excepciones.add(
            ExcepcionAgenda(
                agenda = agenda,
                fechaInicio = dto.fechaInicio,
                fechaFin = dto.fechaFin,
                motivo = dto.motivo.trim()
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
        configs.forEach { config ->
            var actual = LocalDateTime.of(fecha, config.inicioSlot)
            val limite = LocalDateTime.of(fecha, config.finSlot)
            while (!actual.plusMinutes(config.duracionSlotMinutos.toLong()).isAfter(limite)) {
                val finNuevo = actual.plusMinutes(config.duracionSlotMinutos.toLong())
                val ocupado = turnos.any { turno ->
                    val finExistente = turno.iniciaEn.plusMinutes(turno.duracionMinutos.toLong())
                    actual.isBefore(finExistente) && turno.iniciaEn.isBefore(finNuevo)
                }
                slots.add(
                    SlotDto(
                        iniciaEn = actual,
                        duracionMinutos = config.duracionSlotMinutos,
                        disponible = !ocupado
                    )
                )
                actual = finNuevo
            }
        }
        return slots.sortedBy { it.iniciaEn }
    }

    private fun validarAgenda(nombre: String, descripcion: String) {
        if (nombre.trim().isBlank()) {
            throw BusinessException("El nombre de la agenda es obligatorio")
        }
        if (descripcion.trim().isBlank()) {
            throw BusinessException("La descripcion de la agenda es obligatoria")
        }
    }

    private fun validarConfiguracion(dto: ConfiguracionHorariaDto) {
        if (!dto.inicioSlot.isBefore(dto.finSlot)) {
            throw BusinessException("La hora de inicio debe ser anterior a la hora de fin")
        }
        if (dto.duracionSlotMinutos <= 0) {
            throw BusinessException("La duracion del slot debe ser mayor a cero")
        }
        val minutosDisponibles = Duration.between(dto.inicioSlot, dto.finSlot).toMinutes()
        if (dto.duracionSlotMinutos.toLong() > minutosDisponibles) {
            throw BusinessException("La duracion del slot no puede superar el rango horario configurado")
        }
    }

    private fun validarExcepcion(dto: ExcepcionAgendaDto) {
        if (dto.fechaFin.isBefore(dto.fechaInicio)) {
            throw BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio")
        }
        if (dto.motivo.trim().isBlank()) {
            throw BusinessException("El motivo de la excepcion es obligatorio")
        }
    }
}
