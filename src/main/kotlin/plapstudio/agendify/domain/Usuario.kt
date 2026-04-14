package plapstudio.agendify.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "usuarios")
class Usuario(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(unique = true, nullable = false)
    val email: String,

    var contrasenaHash: String,
    var nombreCompleto: String,
    var telefono: String,
    var activo: Boolean = true,
    val creadoEn: LocalDateTime = LocalDateTime.now(),

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_roles",
        joinColumns = [JoinColumn(name = "usuario_id")],
        inverseJoinColumns = [JoinColumn(name = "rol_id")]
    )
    var roles: MutableSet<Rol> = mutableSetOf()
) {
    @OneToOne(mappedBy = "usuario", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    var perfilProfesional: PerfilProfesional? = null

    @OneToOne(mappedBy = "usuario", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    @JsonIgnore
    var perfilCliente: PerfilCliente? = null

    fun tieneRol(nombreRol: String): Boolean =
        roles.any { it.nombre.equals(nombreRol, ignoreCase = true) }

    fun esProfesional() = tieneRol("PROFESIONAL")
    fun esCliente()     = tieneRol("CLIENTE")
    fun esAsistente()   = tieneRol("ASISTENTE")
    fun esAdmin()       = tieneRol("ADMIN")
}
