package plapstudio.agendify.repository

import plapstudio.agendify.domain.Factura
import plapstudio.agendify.domain.PerfilCliente
import plapstudio.agendify.domain.PerfilProfesional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FacturaRepository : JpaRepository<Factura, UUID> {
    fun findByCliente(cliente: PerfilCliente): List<Factura>
    fun findByProfesional(profesional: PerfilProfesional): List<Factura>
}
