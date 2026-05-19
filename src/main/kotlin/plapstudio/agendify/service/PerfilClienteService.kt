package plapstudio.agendify.service

import org.springframework.stereotype.Service
import plapstudio.agendify.domain.PerfilCliente
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.PerfilClienteRepository
import plapstudio.agendify.repository.PerfilProfesionalRepository
import plapstudio.agendify.repository.TurnoRepository

@Service
class PerfilClienteService(
    private val perfilClienteRepository:     PerfilClienteRepository,
    private val perfilProfesionalRepository: PerfilProfesionalRepository,
    private val turnoRepository:             TurnoRepository
) {

    fun findById(id: Long): PerfilCliente =
        perfilClienteRepository.findById(id)
            .orElseThrow { NotFoundException("Cliente no encontrado con id: $id") }

    fun findAll(): List<PerfilCliente> = perfilClienteRepository.findAll()

    fun clientesDeProfesional(profesionalId: Long): List<PerfilCliente> {
        val perfil = perfilProfesionalRepository.findById(profesionalId)
            .orElseThrow { NotFoundException("Profesional no encontrado con id: $profesionalId") }
        return turnoRepository.findByAgendaProfesional(perfil)
            .mapNotNull { it.cliente }
            .distinctBy { it.id }
    }
}
