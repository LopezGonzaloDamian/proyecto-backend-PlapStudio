package plapstudio.agendify.domain

import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(name = "roles")
class Rol(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    var id: UUID? = null,

    @Column(unique = true, nullable = false)
    val nombre: String,

    val descripcion: String = ""
)
