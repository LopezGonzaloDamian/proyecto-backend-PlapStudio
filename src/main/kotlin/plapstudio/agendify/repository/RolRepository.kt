package plapstudio.agendify.repository

import plapstudio.agendify.domain.Rol
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface RolRepository : JpaRepository<Rol, UUID> {
    fun findByNombre(nombre: String): Rol?
}
