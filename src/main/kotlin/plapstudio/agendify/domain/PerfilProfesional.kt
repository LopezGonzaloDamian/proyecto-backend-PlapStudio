package plapstudio.agendify.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.math.BigDecimal

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
    var destacado: Boolean = false,

    var localidad: String = "",
    var direccion: String = "",

    @Column(precision = 12, scale = 2)
    var precio: BigDecimal = BigDecimal.ZERO,

    var cobertura: String = "",
    var matriculaNacional: String = "",
    var matriculaProvincial: String = "",

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "perfil_profesional_servicios",
        joinColumns = [JoinColumn(name = "perfil_profesional_id")]
    )
    @Column(name = "servicio")
    var servicios: MutableList<String> = mutableListOf()
) {
    @Id
    var id: Long? = null
}
