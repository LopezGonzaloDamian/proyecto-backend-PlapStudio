package plapstudio.agendify.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(name = "excepciones_agenda")
class ExcepcionAgenda(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agenda_id", nullable = false)
    @JsonIgnore
    val agenda: Agenda,

    val fechaInicio: LocalDate,
    val fechaFin: LocalDate,
    var motivo: String
)
