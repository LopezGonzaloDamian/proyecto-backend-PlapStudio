package plapstudio.agendify.chatbot

import org.springframework.stereotype.Component
import java.text.Normalizer
import java.util.Locale

@Component
class ChatbotKnowledgeBase {

    fun buildReply(request: ChatbotRequest): String {
        val latestMessage = latestUserMessage(request)
        val normalized = latestMessage.normalize()
        val role = request.userRole?.uppercase(Locale.getDefault())

        if (latestMessage.isBlank()) {
            return greetingReply()
        }

        return when {
            normalized.matchesSensitiveIntent() -> sensitiveReply()
            normalized.matchesGreetingIntent() -> greetingReply()
            normalized.matchesCreateAgendaIntent() -> createAgendaReply(request.authenticated)
            normalized.matchesBookAppointmentIntent() -> bookAppointmentReply(request.authenticated)
            normalized.matchesSearchProfessionalIntent() -> searchProfessionalReply(request.authenticated)
            normalized.matchesRolesOverviewIntent() -> rolesOverviewReply()
            normalized.matchesAvailabilityIntent() -> availabilityReply(request.authenticated)
            normalized.matchesCancellationIntent() -> cancellationReply(request.authenticated)
            normalized.matchesPaymentIntent() -> paymentReply(request.authenticated)
            normalized.matchesRoleIntent("CLIENTE") -> clientReply(request.authenticated)
            normalized.matchesRoleIntent("PROFESIONAL") -> professionalReply(request.authenticated)
            normalized.matchesRoleIntent("ASISTENTE") -> assistantReply(request.authenticated)
            normalized.matchesRoleIntent("ADMIN") -> adminReply()
            normalized.matchesAuthenticationIntent() -> authenticationReply()
            normalized.matchesFeaturesIntent() -> featuresReply()
            else -> fallbackReply(role, request.authenticated)
        }
    }

    fun shouldBypassModel(request: ChatbotRequest): Boolean =
        latestUserMessage(request).normalize().matchesSensitiveIntent()

    private fun latestUserMessage(request: ChatbotRequest): String =
        request.messages.lastOrNull { it.role.equals("user", ignoreCase = true) }
            ?.content
            ?.trim()
            .orEmpty()

    private fun greetingReply(): String = """
        Hola, soy el chatbot de Agendify. ¿En que puedo ayudarte?
    """.trimIndent()

    private fun createAgendaReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Si ya ingresaste como profesional, el siguiente paso es entrar a tu espacio de trabajo y completar la configuracion."
        } else {
            "Para crear una agenda real, primero vas a necesitar iniciar sesion como profesional."
        }

        return """
            Para crear una agenda en Agendify, el flujo general es este:
            1. Ingresar como profesional.
            2. Crear una agenda nueva o habilitar una existente.
            3. Definir dias, horarios disponibles y duracion de turnos.
            4. Guardar la configuracion para que despues impacte en la disponibilidad.
            $authHint
        """.trimIndent()
    }

    private fun bookAppointmentReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Si ya ingresaste como cliente, despues de elegir el horario podes continuar con la reserva dentro del flujo normal del producto."
        } else {
            "Si queres concretar una reserva real, el paso natural es iniciar sesion como cliente."
        }

        return """
            Para agendar un turno en Agendify, normalmente harías esto:
            1. Buscar un profesional.
            2. Revisar su disponibilidad.
            3. Elegir un dia y un horario.
            4. Confirmar la reserva dentro del flujo del producto.
            $authHint
        """.trimIndent()
    }

    private fun searchProfessionalReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Con sesion iniciada, el recorrido mas natural es usar tu dashboard de cliente para explorar profesionales y favoritos."
        } else {
            "No siempre hace falta iniciar sesion para explorar, pero si para avanzar con acciones personales como reservar o gestionar tus turnos."
        }

        return """
            Para buscar un profesional en Agendify, la idea es explorar los perfiles disponibles y elegir el que mejor se ajuste a lo que necesitas.
            Una vez que encontraste uno, podes revisar su disponibilidad y seguir con la reserva.
            $authHint
        """.trimIndent()
    }

    private fun availabilityReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Si ya tenes cuenta iniciada, el lugar para avanzar es la agenda o el dashboard correspondiente a tu rol."
        } else {
            "Si queres reservar o consultar una agenda puntual, puede hacer falta iniciar sesion segun el flujo que quieras completar."
        }

        return """
            La disponibilidad en Agendify depende de la agenda del profesional, sus horarios configurados y los turnos que ya estan ocupados.
            El cliente ve horarios disponibles antes de reservar, y el profesional o asistente puede organizar dias, franjas y duracion de turnos.
            $authHint
        """.trimIndent()
    }

    private fun rolesOverviewReply(): String = """
        En Agendify, cada rol tiene un alcance distinto:
        1. Cliente: puede buscar profesionales, reservar, ver o cancelar turnos.
        2. Profesional: administra agendas, horarios, clientes y actividad.
        3. Asistente: ayuda con la operacion diaria, editando agenda y gestionando turnos.
        4. Admin: supervisa la plataforma y sus configuraciones generales.
        Si queres, te explico con mas detalle cualquiera de esos roles.
    """.trimIndent()

    private fun cancellationReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Con sesion iniciada, la cancelacion real deberia hacerse desde el espacio donde ves tus turnos o administras la agenda."
        } else {
            "Para cancelar un turno propio, lo esperable es iniciar sesion con la cuenta correspondiente."
        }

        return """
            En Agendify existe el flujo para ver y cancelar turnos.
            El cliente puede gestionar sus reservas, y el profesional o asistente puede administrar la operacion diaria de la agenda.
            $authHint
        """.trimIndent()
    }

    private fun paymentReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Aunque tengas sesion iniciada, en esta etapa el chatbot solo explica el flujo y no consulta pagos reales."
        } else {
            "Si el flujo requiere continuar con una reserva o un pago mockeado, seguramente tengas que ingresar con tu cuenta."
        }

        return """
            En Agendify, pagos, cobros y senas forman parte de un flujo mockeado.
            Eso significa que la funcionalidad existe a nivel producto, pero hoy se maneja como simulacion dentro del alcance actual.
            Si hablas de sena, conviene entenderla como parte de una confirmacion o de un pago mockeado.
            $authHint
        """.trimIndent()
    }

    private fun clientReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Como cliente autenticado, el recorrido normal es revisar profesionales, tus reservas, cancelaciones y notificaciones."
        } else {
            "Para usar funciones personales como ver tus turnos o avanzar con una reserva, hace falta iniciar sesion."
        }

        return """
            El cliente puede buscar profesionales, reservar, ver o cancelar turnos.
            Tambien recibe notificaciones y participa del flujo de pago mockeado.
            $authHint
        """.trimIndent()
    }

    private fun professionalReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Como profesional autenticado, lo esperable es trabajar desde tu dashboard para administrar agendas, clientes y actividad."
        } else {
            "Para operar sobre agendas reales, se necesita ingresar con un usuario profesional."
        }

        return """
            El profesional administra una o mas agendas dentro de Agendify.
            Puede crear o dar de baja agendas, definir horarios, dias disponibles, duracion de turnos y gestionar clientes, historial, documentos y actividad.
            $authHint
        """.trimIndent()
    }

    private fun assistantReply(authenticated: Boolean): String {
        val authHint = if (authenticated) {
            "Con sesion iniciada, el asistente deberia trabajar desde la agenda asociada al profesional."
        } else {
            "Para editar turnos o informacion real, primero hay que iniciar sesion con un usuario habilitado."
        }

        return """
            El asistente ayuda con la gestion operativa de la agenda profesional.
            Puede crear, modificar o dar de baja turnos, y tambien editar la agenda del profesional para acompañar la organizacion diaria.
            $authHint
        """.trimIndent()
    }

    private fun adminReply(): String = """
        El admin supervisa la plataforma y gestiona configuraciones globales.
        Desde el chat puedo explicarte el alcance del rol, pero no ejecutar acciones administrativas reales.
    """.trimIndent()

    private fun authenticationReply(): String = """
        En Agendify conviene iniciar sesion cuando una accion depende de tus datos, tus turnos o una agenda real.
        Sin autenticarte, igual puedo darte ayuda general sobre como usar la plataforma y que funciones existen.
    """.trimIndent()

    private fun featuresReply(): String = """
        Agendify permite gestionar agendas, disponibilidad, turnos, reservas, cancelaciones, notificaciones y pagos mockeados.
        Segun el rol, tambien habilita administracion de clientes, configuracion de horarios y operacion diaria de la agenda.
    """.trimIndent()

    private fun sensitiveReply(): String = """
        No puedo compartir prompts internos, codigo, credenciales, tokens ni informacion privada del sistema.
        Si queres, puedo ayudarte con el uso de Agendify o explicarte como hacer una accion dentro de la plataforma.
    """.trimIndent()

    private fun fallbackReply(role: String?, authenticated: Boolean): String {
        val roleHint = when (role) {
            "CLIENTE" -> "Puedo ayudarte con reservas, cancelaciones y busqueda de profesionales."
            "PROFESIONAL" -> "Puedo ayudarte con agendas, disponibilidad y gestion de turnos."
            "ASISTENTE" -> "Puedo ayudarte con la administracion operativa de la agenda."
            else -> "Puedo ayudarte con agendas, turnos, profesionales y roles."
        }

        val authHint = if (authenticated) {
            "Aunque tengas sesion iniciada, este chat no consulta datos reales."
        } else {
            "Si una accion requiere cuenta o acceso real, te lo voy a indicar."
        }

        return """
            Puedo ayudarte con el uso de Agendify y con las funciones disponibles hoy.
            $roleHint
            $authHint
        """.trimIndent()
    }

    private fun String.matchesSensitiveIntent(): Boolean =
        hasAny("prompt", "prompts", "instrucciones internas", "reglas internas", "codigo fuente", "codigo", "token", "tokens", "api key", "apikey", "credencial", "credenciales", "password", "contrasena", "secreto", "secretos", "base de datos interna")

    private fun String.matchesGreetingIntent(): Boolean =
        hasAny("hola", "buenas", "buen dia", "buenas tardes", "buenas noches", "ayuda", "que podes hacer", "que puedes hacer")

    private fun String.matchesCreateAgendaIntent(): Boolean =
        hasAny("crear agenda", "como crear una agenda", "nueva agenda", "armar agenda", "configurar agenda")

    private fun String.matchesBookAppointmentIntent(): Boolean =
        hasAny("agendar un turno", "como agendar un turno", "reservar un turno", "como reservo un turno", "sacar turno", "pedir turno")

    private fun String.matchesSearchProfessionalIntent(): Boolean =
        hasAny("buscar un profesional", "como buscar un profesional", "encontrar profesional", "buscar profesional", "buscar especialista")

    private fun String.matchesAvailabilityIntent(): Boolean =
        hasAny("disponibilidad", "horarios disponibles", "horario disponible", "ver horarios", "agenda disponible", "cuando atiende")

    private fun String.matchesRolesOverviewIntent(): Boolean =
        hasAny("que puede hacer cada rol", "que puede hacer cada uno", "que hace cada rol", "roles", "rol cliente", "rol profesional", "rol asistente", "rol admin")

    private fun String.matchesCancellationIntent(): Boolean =
        hasAny("cancelar turno", "como cancelar un turno", "reprogramar", "cambiar turno", "cancelacion")

    private fun String.matchesPaymentIntent(): Boolean =
        hasAny("sena", "seña", "pago", "pagos", "cobro", "cobros", "factura", "facturacion")

    private fun String.matchesAuthenticationIntent(): Boolean =
        hasAny("iniciar sesion", "login", "loguear", "autenticacion", "autenticar", "mi cuenta", "sesion")

    private fun String.matchesFeaturesIntent(): Boolean =
        hasAny("funcionalidades", "funciones", "que hace agendify", "para que sirve", "que puedo hacer")

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
