package plapstudio.agendify.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(name = "configuracion_horaria")
class ConfiguracionHoraria(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agenda_id", nullable = false)
    @JsonIgnore
    val agenda: Agenda,

    @Enumerated(EnumType.STRING)
    val diaSemana: DayOfWeek,

    var inicioSlot: LocalTime,
    var finSlot: LocalTime,
    var duracionSlotMinutos: Int
)
