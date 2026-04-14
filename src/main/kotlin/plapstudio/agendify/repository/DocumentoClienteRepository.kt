package plapstudio.agendify.repository

import plapstudio.agendify.domain.DocumentoCliente
import plapstudio.agendify.domain.PerfilCliente
import plapstudio.agendify.domain.PerfilProfesional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface DocumentoClienteRepository : JpaRepository<DocumentoCliente, UUID> {
    fun findByCliente(cliente: PerfilCliente): List<DocumentoCliente>
    fun findByProfesional(profesional: PerfilProfesional): List<DocumentoCliente>
}
