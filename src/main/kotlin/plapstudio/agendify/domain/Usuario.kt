package plapstudio.agendify.domain

import jakarta.persistence.*

@Entity
@Table(name = "usuarios")
class Usuario(
    var nombre: String,
    var apellido: String,

    @Column(unique = true, nullable = false)
    val email: String,

    var password: String,
    var telefono: String,

    @Enumerated(EnumType.STRING)
    var rol: Rol
) : Identifiable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Long? = null

    fun esProfesional() = rol == Rol.PROFESIONAL
    fun esCliente()     = rol == Rol.CLIENTE
    fun esAsistente()   = rol == Rol.ASISTENTE
    fun esAdmin()       = rol == Rol.ADMIN
}

//------------------------------------------

enum class Rol {
    ADMIN, PROFESIONAL, ASISTENTE, CLIENTE
}
