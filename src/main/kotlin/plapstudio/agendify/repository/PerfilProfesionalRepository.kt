package plapstudio.agendify.repository

import plapstudio.agendify.domain.PerfilProfesional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PerfilProfesionalRepository : JpaRepository<PerfilProfesional, Long> {
    fun findByDestacadoTrue(): List<PerfilProfesional>
    fun findByEspecialidadContainingIgnoreCase(especialidad: String): List<PerfilProfesional>
}
