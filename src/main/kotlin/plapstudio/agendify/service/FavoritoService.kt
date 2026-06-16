package plapstudio.agendify.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import plapstudio.agendify.domain.Favorito
import plapstudio.agendify.errors.NotFoundException
import plapstudio.agendify.repository.FavoritoRepository
import plapstudio.agendify.repository.PerfilClienteRepository
import plapstudio.agendify.repository.PerfilProfesionalRepository

@Service
class FavoritoService(
    private val favoritoRepository:           FavoritoRepository,
    private val perfilClienteRepository:      PerfilClienteRepository,
    private val perfilProfesionalRepository:  PerfilProfesionalRepository
) {

    fun findByCliente(clienteId: Long): List<Favorito> {
        perfilClienteRepository.findById(clienteId)
            .orElseThrow { NotFoundException("Cliente no encontrado con id: $clienteId") }
        return favoritoRepository.findByClienteId(clienteId)
    }

    @Transactional
    fun toggle(clienteId: Long, profesionalId: Long): Favorito? {
        val cliente = perfilClienteRepository.findById(clienteId)
            .orElseThrow { NotFoundException("Cliente no encontrado con id: $clienteId") }
        val profesional = perfilProfesionalRepository.findById(profesionalId)
            .orElseThrow { NotFoundException("Profesional no encontrado con id: $profesionalId") }
        val existente = favoritoRepository.findByClienteIdAndProfesionalId(clienteId, profesionalId)
        return if (existente != null) {
            favoritoRepository.delete(existente)
            null
        } else {
            favoritoRepository.save(Favorito(cliente = cliente, profesional = profesional))
        }
    }
}
