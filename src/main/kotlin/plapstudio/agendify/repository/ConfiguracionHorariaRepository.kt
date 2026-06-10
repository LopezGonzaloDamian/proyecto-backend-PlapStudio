package plapstudio.agendify.repository

import plapstudio.agendify.domain.Agenda
import plapstudio.agendify.domain.ConfiguracionHoraria
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ConfiguracionHorariaRepository : JpaRepository<ConfiguracionHoraria, UUID> {
    fun findByAgenda(agenda: Agenda): List<ConfiguracionHoraria>
}
