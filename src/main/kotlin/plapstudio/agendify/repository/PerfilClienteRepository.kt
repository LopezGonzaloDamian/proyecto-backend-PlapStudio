package plapstudio.agendify.repository

import plapstudio.agendify.domain.PerfilCliente
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PerfilClienteRepository : JpaRepository<PerfilCliente, Long>
