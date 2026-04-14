package plapstudio.agendify.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "perfiles_profesional")
class PerfilProfesional(
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    val usuario: Usuario,

    var especialidad: String,
    var biografia: String = "",
    var urlAvatar: String = "",
    var destacado: Boolean = false
) {
    @Id
    var id: Long? = null
}
