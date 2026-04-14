package plapstudio.agendify.repository

import plapstudio.agendify.domain.Favorito
import plapstudio.agendify.domain.PerfilCliente
import plapstudio.agendify.domain.PerfilProfesional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FavoritoRepository : JpaRepository<Favorito, UUID> {
    fun findByCliente(cliente: PerfilCliente): List<Favorito>
    fun existsByClienteAndProfesional(cliente: PerfilCliente, profesional: PerfilProfesional): Boolean
}
