package plapstudio.agendify.domain

import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalTime

@Entity
@Table(name = "agendas")
class Agenda(
    var nombre: String,
    var descripcion: String,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profesional_id", nullable = false)
    val profesional: Usuario,

    @ElementCollection(fetch = FetchType.EAGER, targetClass = DayOfWeek::class)
    @CollectionTable(name = "agenda_dias", joinColumns = [JoinColumn(name = "agenda_id")])
    @Enumerated(EnumType.STRING)
    @Column(name = "dia")
    var diasDisponibles: MutableSet<DayOfWeek> = mutableSetOf(),

    var horaInicio: LocalTime,
    var horaFin: LocalTime,
    var duracionTurnoMinutos: Int,
    var activa: Boolean = true
) : Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Long? = null

    fun estaDisponibleEn(dia: DayOfWeek, hora: LocalTime): Boolean =
        activa &&
        dia in diasDisponibles &&
        !hora.isBefore(horaInicio) &&
        !hora.plusMinutes(duracionTurnoMinutos.toLong()).isAfter(horaFin)

    fun darDeBaja() {
        activa = false
    }
}
