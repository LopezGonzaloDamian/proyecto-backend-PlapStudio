package plapstudio.agendify.chatbot

import org.springframework.stereotype.Component
import java.text.Normalizer
import java.util.Locale

@Component
class ChatbotKnowledgeBase {

    fun buildReply(request: ChatbotRequest): String {
        val latestMessage = request.messages.lastOrNull { it.role.equals("user", ignoreCase = true) }
            ?.content
            ?.trim()
            .orEmpty()

        val normalized = latestMessage.normalize()
        val role = request.userRole?.uppercase(Locale.getDefault())

        if (latestMessage.isBlank()) {
            return greetingFor(role, request.authenticated)
        }

        return when {
            normalized.matchesHelpIntent() -> greetingFor(role, request.authenticated)
            normalized.matchesAvailabilityIntent() -> availabilityReply(request.authenticated)
            normalized.matchesBookingIntent() -> bookingReply(request.authenticated)
            normalized.matchesPaymentIntent() -> paymentReply(request.authenticated)
            normalized.matchesStatusIntent() -> statusReply(request.authenticated)
            normalized.matchesRoleIntent("CLIENTE") -> clientReply(request.authenticated)
            normalized.matchesRoleIntent("PROFESIONAL") -> professionalReply(request.authenticated)
            normalized.matchesRoleIntent("ASISTENTE") -> assistantReply(request.authenticated)
            normalized.matchesRoleIntent("ADMIN") -> adminReply()
            normalized.matchesAuthenticationIntent() -> authenticationReply()
            else -> fallbackReply(role, request.authenticated)
        }
    }

    private fun greetingFor(role: String?, authenticated: Boolean): String {
        val roleLine = when (role) {
            "CLIENTE" -> "Puedo orientarte como cliente para buscar disponibilidad, reservar o cancelar turnos."
            "PROFESIONAL" -> "Puedo orientarte como profesional para gestionar agendas, horarios, clientes y actividad."
            "ASISTENTE" -> "Puedo orientarte como asistente para editar la agenda y administrar turnos del profesional."
            "ADMIN" -> "Puedo orientarte con el alcance general del sistema y los roles de Agendify."
            else -> "Puedo orientarte sobre reservas, disponibilidad, pagos mockeados y roles dentro de Agendify."
        }

        val authLine = if (authenticated) {
            "Veo tu consulta como usuario autenticado, pero esta demo responde con ayuda guiada y no accede a datos personales."
        } else {
            "Si una accion requiere cuenta o datos reales, te lo voy a indicar claramente."
        }

        return """
            Hola, soy el asistente de Agendify.
            $roleLine
            $authLine
            Podes preguntarme por disponibilidad, reservas, estados, pagos mockeados o que puede hacer cada rol.
        """.trimIndent()
    }

    private fun availabilityReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Si ya tenes sesion iniciada, el siguiente paso seria entrar a la agenda o al dashboard correspondiente."
        } else {
            "Para consultar una agenda especifica o continuar una reserva real, probablemente necesites iniciar sesion."
        }

        return """
            En Agendify la disponibilidad se basa en la agenda del profesional, sus horarios configurados y los turnos ya ocupados.
            Un cliente puede ver horarios disponibles antes de reservar.
            Un profesional o asistente puede definir dias, franjas horarias y duracion de turnos.
            $authHint
        """.trimIndent()
    }

    private fun bookingReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Como esta es una guia estatica, no puedo crear el turno desde el chat, pero si indicarte el flujo correcto."
        } else {
            "Si queres avanzar con una reserva real, primero deberias iniciar sesion como cliente."
        }

        return """
            El flujo de reserva en Agendify es: buscar un profesional, revisar disponibilidad y elegir un turno.
            Despues se confirma la solicitud dentro del flujo del producto.
            El cliente puede visualizar o cancelar sus turnos, y el profesional gestiona lo que ocurre en su agenda.
            $authHint
        """.trimIndent()
    }

    private fun paymentReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "La wiki describe pagos y cobros online en modo mock, asi que el chatbot solo orienta sobre ese flujo."
        } else {
            "Si la reserva requiere continuar con un pago mockeado, el sistema deberia pedirte ingresar con tu cuenta."
        }

        return """
            Segun la wiki, Agendify contempla pagos, facturacion y cobros online en modo mock.
            Eso significa que existe el flujo de negocio, pero en esta demo se trata como una simulacion del proceso.
            Si hablas de sena, hoy conviene entenderla como parte de ese flujo de pago o confirmacion mockeada.
            $authHint
        """.trimIndent()
    }

    private fun statusReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Con cuenta iniciada, el dashboard correspondiente seria el lugar para revisar estados reales."
        } else {
            "Sin sesion iniciada, el chatbot solo puede darte una explicacion general del flujo."
        }

        return """
            Un turno puede atravesar distintos estados operativos, por ejemplo reserva activa, cancelacion o confirmacion dentro del flujo del sistema.
            El cliente consulta sus turnos y notificaciones, mientras que el profesional o asistente administra la operacion diaria.
            Agendify tambien centraliza pagos mockeados, recordatorios y actividad relacionada.
            $authHint
        """.trimIndent()
    }

    private fun clientReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Como cliente autenticado, el recorrido natural es revisar tu dashboard, favoritos y tus turnos."
        } else {
            "Si queres usar funciones personales del cliente, tenes que iniciar sesion."
        }

        return """
            El cliente en Agendify puede buscar profesionales, reservar, visualizar o cancelar turnos.
            Tambien recibe notificaciones y participa del flujo de pago online mockeado.
            El objetivo es que la reserva se haga desde una experiencia digital centralizada y simple.
            $authHint
        """.trimIndent()
    }

    private fun professionalReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Como profesional autenticado, el recorrido natural es administrar tu agenda y revisar clientes, pagos y actividad."
        } else {
            "Para operar sobre agendas reales, Agendify requiere iniciar sesion."
        }

        return """
            El profesional es dueño de una o mas agendas y administra su operacion diaria dentro de Agendify.
            Puede crear o dar de baja agendas, definir horarios, dias disponibles y duracion de turnos.
            Tambien gestiona clientes, historial, documentos, notificaciones, pagos y actividad general.
            $authHint
        """.trimIndent()
    }

    private fun assistantReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Con sesion iniciada, el asistente deberia trabajar desde la agenda asociada al profesional."
        } else {
            "Si necesitas editar informacion real, primero deberias ingresar con un usuario habilitado."
        }

        return """
            El asistente colabora con la gestion operativa de una agenda profesional.
            Puede crear, modificar o dar de baja turnos y tambien editar la agenda del profesional.
            Su funcion es ayudar en la organizacion diaria de pacientes o clientes sin asumir el rol de administrador general.
            $authHint
        """.trimIndent()
    }

    private fun adminReply(): String = """
        El admin supervisa la plataforma y gestiona configuraciones globales del SaaS.
        Su alcance es transversal al sistema, no solo sobre una agenda puntual.
        En esta demo el chatbot puede explicarte el rol, pero no ejecutar acciones administrativas reales desde el chat.
    """.trimIndent()

    private fun authenticationReply(): String = """
        En Agendify conviene iniciar sesion cuando una accion depende de tus datos reales, tu agenda o tus turnos.
        Sin autenticarte, el chatbot puede orientarte con ayuda general y explicar el flujo.
        Si queres reservar, revisar un turno tuyo o gestionar una agenda real, lo esperado es entrar con tu cuenta.
    """.trimIndent()

    private fun fallbackReply(role: String?, authenticated: Boolean): String {
        val roleHint = when (role) {
            "CLIENTE" -> "Si queres, puedo explicarte como reservar o cancelar un turno."
            "PROFESIONAL" -> "Si queres, puedo explicarte como administrar horarios o clientes."
            "ASISTENTE" -> "Si queres, puedo explicarte como editar turnos o ayudar con la agenda."
            else -> "Si queres, puedo orientarte sobre disponibilidad, reservas, roles o pagos mockeados."
        }

        val authHint = if (authenticated) {
            "Aunque tengas sesion iniciada, esta version del chatbot responde con guias estaticas y no consulta datos reales."
        } else {
            "Si tu consulta depende de informacion personal o de una cuenta, te voy a indicar cuando haga falta iniciar sesion."
        }

        return """
            Puedo ayudarte con el alcance actual de Agendify segun la wiki del proyecto.
            $roleHint
            $authHint
        """.trimIndent()
    }

    private fun String.matchesHelpIntent(): Boolean =
        hasAny("hola", "buenas", "ayuda", "help", "que podes hacer", "que puedes hacer", "que haces")

    private fun String.matchesAvailabilityIntent(): Boolean =
        hasAny("disponibilidad", "disponible", "horario", "horarios", "agenda", "turnos disponibles", "cuando atiende")

    private fun String.matchesBookingIntent(): Boolean =
        hasAny("reserv", "sacar turno", "pedir turno", "agendar", "cancelar", "reprogram")

    private fun String.matchesPaymentIntent(): Boolean =
        hasAny("sena", "seña", "pago", "pagos", "cobro", "cobros", "factura", "facturacion")

    private fun String.matchesStatusIntent(): Boolean =
        hasAny("estado", "estados", "confirm", "pendiente", "completado", "cancelado", "notificacion", "recordatorio")

    private fun String.matchesAuthenticationIntent(): Boolean =
        hasAny("iniciar sesion", "login", "logue", "autentic", "cuenta", "sesion")

    private fun String.matchesRoleIntent(expectedRole: String): Boolean = when (expectedRole) {
        "CLIENTE" -> hasAny("cliente", "paciente", "usuario final")
        "PROFESIONAL" -> hasAny("profesional", "especialista", "doctor", "medico", "nutricionista", "psicologo", "psicologa")
        "ASISTENTE" -> hasAny("asistente", "secretaria", "secretario")
        "ADMIN" -> hasAny("admin", "administrador")
        else -> false
    }

    private fun String.hasAny(vararg values: String): Boolean =
        values.any { contains(it) }

    private fun String.normalize(): String =
        Normalizer.normalize(lowercase(Locale.getDefault()), Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
}
