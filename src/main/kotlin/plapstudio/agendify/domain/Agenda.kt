package plapstudio.agendify.domain

import jakarta.persistence.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(name = "agendas")
class Agenda(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "email_profesional", nullable = false)
    val profesional: PerfilProfesional,

    var nombre: String,
    var descripcion: String,
    var activa: Boolean = true,
    val creadaEn: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "agenda", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    var configuraciones: MutableList<ConfiguracionHoraria> = mutableListOf(),

    @OneToMany(mappedBy = "agenda", cascade = [CascadeType.ALL], fetch = FetchType.EAGER, orphanRemoval = true)
    var excepciones: MutableList<ExcepcionAgenda> = mutableListOf()
) {
    fun estaDisponibleEn(dia: DayOfWeek, hora: LocalTime, duracionMinutos: Int): Boolean {
        if (!activa) return false
        return configuraciones.any { config ->
            config.diaSemana == dia &&
            !hora.isBefore(config.inicioSlot) &&
            !hora.plusMinutes(duracionMinutos.toLong()).isAfter(config.finSlot)
        }
    }

    fun tieneExcepcionEn(fecha: LocalDate): Boolean =
        excepciones.any { !fecha.isBefore(it.fechaInicio) && !fecha.isAfter(it.fechaFin) }

    fun darDeBaja() {
        activa = false
    }
}
