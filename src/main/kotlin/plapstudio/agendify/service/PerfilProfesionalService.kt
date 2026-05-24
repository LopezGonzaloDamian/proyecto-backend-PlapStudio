package plapstudio.agendify.service

import org.springframework.stereotype.Service
import plapstudio.agendify.domain.Agenda
import plapstudio.agendify.domain.PerfilProfesional
import plapstudio.agendify.dto.ProfesionalUpdateRequest
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.AgendaRepository
import plapstudio.agendify.repository.PerfilProfesionalRepository

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

    fun buscar(query: String?, especialidad: String?, localidad: String?): List<PerfilProfesional> {
        val q  = query?.trim()?.lowercase().orEmpty()
        val es = especialidad?.trim()?.lowercase().orEmpty()
        val ub = localidad?.trim()?.lowercase().orEmpty()
        return perfilProfesionalRepository.findAll().filter { p ->
            val coincideQuery =
                q.isEmpty() ||
                p.usuario.nombreCompleto.lowercase().contains(q) ||
                p.especialidad.lowercase().contains(q) ||
                p.servicios.any { it.lowercase().contains(q) }
            val coincideEs =
                es.isEmpty() ||
                p.especialidad.lowercase().contains(es) ||
                p.servicios.any { it.lowercase().contains(es) }
            val coincideUb = ub.isEmpty() || p.localidad.lowercase().contains(ub)
            coincideQuery && coincideEs && coincideUb
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
        perfil.servicios           = req.servicios.toMutableList()
        return perfilProfesionalRepository.save(perfil)
    }
}
