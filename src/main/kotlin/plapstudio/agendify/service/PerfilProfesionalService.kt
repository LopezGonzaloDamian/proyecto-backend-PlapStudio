package plapstudio.agendify.service

import org.springframework.stereotype.Service
import plapstudio.agendify.domain.Agenda
import plapstudio.agendify.domain.PerfilProfesional
import plapstudio.agendify.domain.ServicioProfesional
import plapstudio.agendify.dto.ProfesionalUpdateRequest
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.AgendaRepository
import plapstudio.agendify.repository.PerfilProfesionalRepository
import java.math.BigDecimal
import java.time.LocalDate

@Service
class PerfilProfesionalService(
    private val perfilProfesionalRepository: PerfilProfesionalRepository,
    private val agendaRepository:            AgendaRepository
) {

    fun findById(id: Long): PerfilProfesional =
        perfilProfesionalRepository.findById(id)
            .orElseThrow { NotFoundException("Profesional no encontrado con id: $id") }

    fun findAll(): List<PerfilProfesional> = perfilProfesionalRepository.findAll()

    fun findDestacados(): List<PerfilProfesional> =
        perfilProfesionalRepository.findByDestacadoTrue()

    fun buscar(query: String?, especialidad: String?, localidad: String?, fechaDeseada: LocalDate?): List<PerfilProfesional> {
        val q  = query?.trim()?.lowercase().orEmpty()
        val es = especialidad?.trim()?.lowercase().orEmpty()
        val ub = localidad?.trim()?.lowercase().orEmpty()
        return perfilProfesionalRepository.findAll().filter { p ->
            val coincideQuery =
                q.isEmpty() ||
                p.usuario.nombreCompleto.lowercase().contains(q) ||
                p.especialidad.lowercase().contains(q) ||
                p.servicios.any { it.lowercase().contains(q) } ||
                p.serviciosConPrecio.any { it.nombre.lowercase().contains(q) }
            val coincideEs =
                es.isEmpty() ||
                p.especialidad.lowercase().contains(es) ||
                p.servicios.any { it.lowercase().contains(es) } ||
                p.serviciosConPrecio.any { it.nombre.lowercase().contains(es) }
            val coincideUb = ub.isEmpty() || p.localidad.lowercase().contains(ub)
            val coincideFecha = fechaDeseada == null || agendaRepository.findByProfesional(p).any { agenda ->
                agenda.activa &&
                !agenda.tieneExcepcionEn(fechaDeseada) &&
                agenda.configuraciones.any { it.diaSemana == fechaDeseada.dayOfWeek }
            }
            coincideQuery && coincideEs && coincideUb && coincideFecha
        }
    }

    fun agendasDe(profesionalId: Long): List<Agenda> {
        val perfil = findById(profesionalId)
        return agendaRepository.findByProfesional(perfil)
    }

    fun update(id: Long, req: ProfesionalUpdateRequest): PerfilProfesional {
        val perfil = findById(id)
        perfil.especialidad        = req.especialidad
        perfil.biografia           = req.biografia
        perfil.urlAvatar           = req.urlAvatar
        perfil.localidad           = req.localidad
        perfil.direccion           = req.direccion
        perfil.precio              = req.precio
        perfil.cobertura           = req.cobertura
        perfil.matriculaNacional   = req.matriculaNacional
        perfil.matriculaProvincial = req.matriculaProvincial
        val serviciosConPrecio = req.serviciosConPrecio
            .map { ServicioProfesional(it.nombre.trim(), it.precio) }
            .filter { it.nombre.isNotBlank() && it.precio > BigDecimal.ZERO }

        perfil.servicios = if (serviciosConPrecio.isNotEmpty()) {
            serviciosConPrecio.map { it.nombre }.toMutableList()
        } else {
            req.servicios.map { it.trim() }.filter { it.isNotBlank() }.toMutableList()
        }
        perfil.serviciosConPrecio = if (serviciosConPrecio.isNotEmpty()) {
            serviciosConPrecio.toMutableList()
        } else {
            perfil.servicios.map { ServicioProfesional(it, req.precio) }.toMutableList()
        }
        return perfilProfesionalRepository.save(perfil)
    }
}
