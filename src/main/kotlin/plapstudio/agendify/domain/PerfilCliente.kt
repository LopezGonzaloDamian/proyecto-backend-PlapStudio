package plapstudio.agendify.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "perfiles_cliente")
class PerfilCliente(
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    @JsonIgnore
    val usuario: Usuario,

    var notas: String = ""
) {
    @Id
    var id: Long? = null
}
