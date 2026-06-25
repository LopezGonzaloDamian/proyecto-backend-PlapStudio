package plapstudio.agendify.chatbot

import org.springframework.stereotype.Component
import java.text.Normalizer
import java.util.Locale

@Component
class ChatbotKnowledgeBase {

    companion object {
        const val MAIN_MENU_ID = "main_menu"
    }

    private val nodes = buildNodes()
    private val parentByNodeId = buildParentMap(nodes)

    fun buildReply(request: ChatbotRequest): ChatbotReplyDraft {
        val latestMessage = latestUserMessage(request)
        val normalized = latestMessage.normalize()

        request.action?.let { action ->
            return handleAction(action, request)
        }

        if (latestMessage.isBlank() || normalized.matchesGreetingIntent()) {
            return mainMenuReply()
        }

        if (normalized.matchesSensitiveIntent()) {
            return secureReply()
        }

        if (normalized.matchesPersonalDataIntent()) {
            return personalDataReply(normalized)
        }

        val matchedNode = resolveIntentNode(normalized)
        if (matchedNode != null) {
            return nodeReply(
                node = matchedNode,
                source = "intent",
                allowHumanization = matchedNode.humanizableFromText
            )
        }

        return fallbackToMenu()
    }

    fun shouldBypassModel(reply: ChatbotReplyDraft): Boolean =
        reply.source in setOf("menu", "fallback", "secure", "personal-data")

    private fun handleAction(action: ChatbotActionRequest, request: ChatbotRequest): ChatbotReplyDraft =
        when (action.type) {
            ChatbotOptionType.MAIN_MENU -> mainMenuReply()
            ChatbotOptionType.BACK, ChatbotOptionType.OPEN_NODE -> {
                val targetId = action.targetNodeId ?: MAIN_MENU_ID
                val node = nodes[targetId] ?: nodes.getValue(MAIN_MENU_ID)
                nodeReply(node = node, source = "menu", allowHumanization = false)
            }

            ChatbotOptionType.TEXT_INTENT -> {
                val payload = action.payload?.takeIf { it.isNotBlank() }
                if (payload.isNullOrBlank()) {
                    fallbackToMenu()
                } else {
                    buildReply(
                        request.copy(
                            action = null,
                            messages = request.messages + ChatbotMessage(role = "user", content = payload)
                        )
                    )
                }
            }
        }

    private fun mainMenuReply(): ChatbotReplyDraft {
        val root = nodes.getValue(MAIN_MENU_ID)
        return ChatbotReplyDraft(
            message = root.message,
            source = "menu",
            options = root.children.map(::menuOptionFor),
            currentNodeId = root.id,
            previousNodeId = null,
            allowHumanization = false
        )
    }

    private fun fallbackToMenu(): ChatbotReplyDraft =
        ChatbotReplyDraft(
            message = "No estoy seguro de haber entendido tu consulta. Podés elegir una categoría para orientarte mejor.",
            source = "fallback",
            options = nodes.getValue(MAIN_MENU_ID).children.map(::menuOptionFor),
            currentNodeId = MAIN_MENU_ID,
            previousNodeId = null,
            allowHumanization = false
        )

    private fun secureReply(): ChatbotReplyDraft =
        ChatbotReplyDraft(
            message = "No puedo ayudarte con credenciales, código, prompts internos ni información privada del sistema. Si querés, sí puedo orientarte sobre cómo usar Agendify.",
            source = "secure",
            options = nodes.getValue(MAIN_MENU_ID).children.map(::menuOptionFor),
            currentNodeId = MAIN_MENU_ID,
            previousNodeId = null,
            allowHumanization = false
        )

    private fun personalDataReply(normalizedMessage: String): ChatbotReplyDraft {
        val message = when {
            normalizedMessage.hasAny("reserva hoy", "mis reservas", "mis turnos", "que reservas tengo", "que turnos tengo") ->
                "Para ver tus reservas reales, ingresá a la sección Mis reservas. Desde ahí vas a poder consultar tus turnos activos, pendientes o cancelados."

            normalizedMessage.hasAny("quien me reservo", "quien reservó", "quien me reservó", "reservo manana", "reservó mañana", "quien reservo manana") ->
                "Para ver las reservas reales de tu agenda, ingresá al panel del profesional y revisá la sección de reservas o agenda."

            else ->
                "Para ver información real de tu cuenta o de tu agenda, ingresá a la sección correspondiente dentro de Agendify."
        }

        return ChatbotReplyDraft(
            message = message,
            source = "personal-data",
            options = listOf(
                menuOptionFor("reserve_my_reservations"),
                menuOptionFor("support_menu"),
                mainMenuOption()
            ),
            currentNodeId = MAIN_MENU_ID,
            previousNodeId = null,
            allowHumanization = false
        )
    }

    private fun nodeReply(
        node: ChatbotMenuNode,
        source: String,
        allowHumanization: Boolean
    ): ChatbotReplyDraft {
        val previousNodeId = parentByNodeId[node.id]

        val primaryOptions = when {
            node.id == MAIN_MENU_ID -> node.children.map(::menuOptionFor)
            node.children.isNotEmpty() -> node.children.map(::menuOptionFor) + navigationOptions(previousNodeId)
            else -> navigationOptions(previousNodeId)
        }

        val suggestedActions = node.relatedNodeIds
            .distinct()
            .mapNotNull(nodes::get)
            .map { relatedNode ->
                ChatbotOption(
                    id = relatedNode.id,
                    label = relatedNode.title,
                    type = ChatbotOptionType.OPEN_NODE,
                    targetNodeId = relatedNode.id
                )
            }
            .take(4)

        return ChatbotReplyDraft(
            message = node.message,
            source = source,
            options = primaryOptions,
            currentNodeId = node.id,
            previousNodeId = previousNodeId,
            suggestedActions = if (node.id == MAIN_MENU_ID) emptyList() else suggestedActions,
            allowHumanization = allowHumanization,
            factualGuide = node.message
        )
    }

    private fun navigationOptions(previousNodeId: String?): List<ChatbotOption> {
        val backTarget = previousNodeId ?: MAIN_MENU_ID

        return listOf(
            ChatbotOption(
                id = "back-$backTarget",
                label = "Volver",
                type = ChatbotOptionType.BACK,
                targetNodeId = backTarget
            ),
            mainMenuOption()
        )
    }

    private fun mainMenuOption() = ChatbotOption(
        id = "main-menu",
        label = "Menú inicial",
        type = ChatbotOptionType.MAIN_MENU,
        targetNodeId = MAIN_MENU_ID
    )

    private fun menuOptionFor(nodeId: String): ChatbotOption {
        val node = nodes.getValue(nodeId)
        return ChatbotOption(
            id = node.id,
            label = node.title,
            type = ChatbotOptionType.OPEN_NODE,
            targetNodeId = node.id
        )
    }

    private fun resolveIntentNode(normalizedMessage: String): ChatbotMenuNode? =
        nodes.values
            .asSequence()
            .filter { it.keywords.isNotEmpty() }
            .sortedByDescending { it.keywords.maxOfOrNull(String::length) ?: 0 }
            .firstOrNull { node -> node.keywords.any { normalizedMessage.matchesKeyword(it) } }

    private fun latestUserMessage(request: ChatbotRequest): String =
        request.messages.lastOrNull { it.role.equals("user", ignoreCase = true) }
            ?.content
            ?.trim()
            .orEmpty()

    private fun buildNodes(): Map<String, ChatbotMenuNode> {
        val allNodes = listOf(
            ChatbotMenuNode(
                id = MAIN_MENU_ID,
                title = "Menú principal",
                message = "Hola, soy el asistente de Agendify. ¿En qué puedo ayudarte?",
                children = listOf(
                    "reserve_menu",
                    "specialist_menu",
                    "payments_menu",
                    "roles_menu",
                    "features_menu",
                    "account_menu",
                    "support_menu"
                )
            ),
            ChatbotMenuNode(
                id = "reserve_menu",
                title = "Reservar turnos",
                message = "Estas son las consultas más comunes para reservar y administrar tus turnos.",
                children = listOf(
                    "reserve_how_to",
                    "reserve_availability",
                    "reserve_no_slots",
                    "reserve_my_reservations",
                    "reserve_cancel",
                    "reserve_statuses"
                ),
                keywords = setOf("reservar turno", "sacar turno", "agendar turno", "pedir turno", "turno")
            ),
            ChatbotMenuNode(
                id = "reserve_how_to",
                title = "Cómo reservar un turno",
                message = "Para reservar un turno, ingresá al perfil del especialista, revisá los horarios disponibles, elegí el que prefieras y confirmá la reserva. Si el sistema solicita una seña, el turno queda confirmado cuando el pago se registra correctamente.",
                relatedNodeIds = listOf("reserve_availability", "payments_how_sena", "reserve_my_reservations"),
                keywords = setOf("como reservar un turno", "reservar turno", "sacar un turno", "agendar turno", "pedir turno", "quiero un turn"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "reserve_availability",
                title = "Cómo ver disponibilidad",
                message = "La disponibilidad se consulta desde el perfil del especialista o desde la agenda que aparece al reservar. Ahí vas a poder ver los horarios libres para elegir fecha y hora.",
                relatedNodeIds = listOf("reserve_how_to", "reserve_no_slots", "reserve_my_reservations"),
                keywords = setOf("como ver disponibilidad", "ver disponibilidad", "horarios disponibles", "horario disponible", "ver horarios", "disponibilidad"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "reserve_no_slots",
                title = "Qué hacer si no hay horarios disponibles",
                message = "Si no aparecen horarios disponibles, puede deberse a que el especialista todavía no configuró su agenda, no tiene cupos libres para la fecha elegida o esos turnos ya fueron reservados. Probá cambiar la fecha o volver a intentar más tarde.",
                relatedNodeIds = listOf("reserve_availability", "reserve_how_to"),
                keywords = setOf("no hay horarios", "no veo horarios", "sin horarios disponibles", "no aparece disponibilidad", "no encuentro horarios", "no veo turn"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "reserve_my_reservations",
                title = "Cómo ver mis reservas",
                message = "Para ver tus reservas, ingresá con tu cuenta y revisá la sección Mis reservas o el listado de turnos. Desde ahí vas a poder consultar el estado de cada turno y gestionar las acciones disponibles.",
                relatedNodeIds = listOf("reserve_cancel", "reserve_statuses", "account_login"),
                keywords = setOf("ver mis reservas", "mis reservas", "mis turnos", "donde veo mis reservas", "donde veo mis turnos"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "reserve_cancel",
                title = "Cómo cancelar una reserva",
                message = "Para cancelar una reserva, ingresá a tu listado de turnos, abrí la reserva correspondiente y elegí la opción de cancelación. Una vez cancelada, el turno deja de estar activo y el especialista recibe la notificación correspondiente.",
                relatedNodeIds = listOf("payments_cancel_reservation", "reserve_my_reservations", "reserve_statuses"),
                keywords = setOf("como cancelar una reserva", "cancelar reserva", "cancelar turno", "anular turno", "anular reserva", "como anular una cita"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "reserve_statuses",
                title = "Estados de una reserva",
                message = "Una reserva puede estar pendiente, confirmada o cancelada. Pendiente indica que el proceso todavía no terminó, confirmada que el turno quedó aceptado y cancelada que la reserva dejó de estar activa.",
                relatedNodeIds = listOf("payments_pending", "payments_turn_confirmed", "reserve_cancel"),
                keywords = setOf("estados de una reserva", "estado del turno", "turno pendiente", "turno confirmado", "turno cancelado", "que significa pendiente", "que significa confirmado", "que significa cancelado", "reserva pendiente"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "specialist_menu",
                title = "Para especialistas",
                message = "Estas son las consultas guiadas para profesionales y especialistas que administran su agenda en Agendify.",
                children = listOf(
                    "specialist_config_agenda",
                    "specialist_create_slots",
                    "specialist_received_reservations",
                    "specialist_change_status",
                    "specialist_internal_notes",
                    "specialist_manage_availability"
                ),
                keywords = setOf("soy especialista", "soy profesional", "profesional", "especialista", "agenda profesional")
            ),
            ChatbotMenuNode(
                id = "specialist_config_agenda",
                title = "Cómo configurar mi agenda",
                message = "Para configurar tu agenda, ingresá como profesional, creá o habilitá tu agenda y definí días, franjas horarias y duración de los turnos. Una vez guardada la configuración, esa disponibilidad queda publicada para las reservas.",
                relatedNodeIds = listOf("specialist_create_slots", "specialist_manage_availability"),
                keywords = setOf("como configurar mi agenda", "configurar agenda", "crear agenda", "nueva agenda"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "specialist_create_slots",
                title = "Cómo crear horarios disponibles",
                message = "Los horarios disponibles se crean dentro de tu agenda profesional, definiendo días, bloques horarios y duración de turnos. A partir de eso, los clientes ven únicamente los espacios que quedaron disponibles.",
                relatedNodeIds = listOf("specialist_config_agenda", "specialist_manage_availability"),
                keywords = setOf("como crear horarios disponibles", "crear disponibilidad", "crear horarios", "publicar horarios"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "specialist_received_reservations",
                title = "Cómo ver reservas recibidas",
                message = "Las reservas recibidas se consultan desde la agenda o desde el panel operativo del profesional. Desde ahí podés revisar los turnos tomados, sus estados y las acciones disponibles.",
                relatedNodeIds = listOf("specialist_change_status", "specialist_internal_notes"),
                keywords = setOf("como ver reservas recibidas", "ver reservas recibidas", "ver turnos recibidos", "turnos que me reservaron"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "specialist_change_status",
                title = "Cómo cambiar el estado de un turno",
                message = "Para cambiar el estado de un turno, ingresá a la gestión del turno dentro de la agenda y elegí la acción correspondiente. Desde ahí podés pasar una reserva por estados como pendiente, confirmada o cancelada.",
                relatedNodeIds = listOf("specialist_received_reservations", "reserve_statuses"),
                keywords = setOf("como cambiar el estado de un turno", "cambiar estado del turno", "actualizar estado del turno"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "specialist_internal_notes",
                title = "Cómo usar notas internas",
                message = "Las notas internas sirven para dejar seguimiento operativo y observaciones útiles dentro del trabajo diario. Lo habitual es usarlas desde el contexto del turno, del cliente o de la agenda.",
                relatedNodeIds = listOf("specialist_received_reservations", "specialist_change_status"),
                keywords = setOf("como usar notas internas", "notas internas", "agregar notas internas", "nota interna"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "specialist_manage_availability",
                title = "Cómo administrar mi disponibilidad",
                message = "Administrar la disponibilidad implica ajustar días, horarios y bloques de tu agenda para reflejar cuándo podés atender. Cada cambio impacta en los horarios que luego ven los clientes al reservar.",
                relatedNodeIds = listOf("specialist_config_agenda", "specialist_create_slots"),
                keywords = setOf("como administrar mi disponibilidad", "administrar disponibilidad", "gestionar disponibilidad", "manejar horarios"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "payments_menu",
                title = "Pagos y señas",
                message = "Acá tenés las consultas guiadas sobre pagos, señas y confirmación de reservas.",
                children = listOf(
                    "payments_how_sena",
                    "payments_turn_confirmed",
                    "payments_pending",
                    "payments_cancel_reservation",
                    "payments_mocked"
                ),
                keywords = setOf("pagos", "seña", "senia", "anticipo", "pago", "cobro")
            ),
            ChatbotMenuNode(
                id = "payments_how_sena",
                title = "Cómo funciona la seña",
                message = "La seña permite confirmar la reserva del turno. Una vez realizado el pago, la reserva pasa a estado confirmado y el especialista puede verla reflejada en su agenda.",
                relatedNodeIds = listOf("payments_turn_confirmed", "payments_pending", "reserve_how_to"),
                keywords = setOf("como funciona la sena", "como funciona la seña", "seña", "anticipo", "pago anticipado"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "payments_turn_confirmed",
                title = "Cuándo queda confirmado el turno",
                message = "El turno queda confirmado una vez que la reserva se completa y, si correspondía, la seña se registra correctamente. Desde ese momento, el horario queda tomado en la agenda del especialista.",
                relatedNodeIds = listOf("payments_how_sena", "reserve_statuses"),
                keywords = setOf("cuando queda confirmado el turno", "cuándo queda confirmado el turno", "turno confirmado", "cuando se confirma la reserva"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "payments_pending",
                title = "Qué pasa si el pago queda pendiente",
                message = "Si el pago queda pendiente, la reserva puede mantenerse en estado pendiente hasta que la operación se confirme. Una vez acreditado el pago, el turno pasa a estado confirmado.",
                relatedNodeIds = listOf("payments_how_sena", "reserve_statuses", "support_help_reservation"),
                keywords = setOf("pago pendiente", "que pasa si el pago queda pendiente", "qué pasa si el pago queda pendiente", "quedo pendiente el pago", "estado del pago"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "payments_cancel_reservation",
                title = "Qué pasa si cancelo una reserva",
                message = "Cuando cancelás una reserva, el turno deja de estar activo y el especialista recibe una notificación de cancelación. Si habías realizado una seña, el reintegro se procesa al mismo medio de pago utilizado para confirmar la reserva. Después podés reservar otro horario disponible.",
                relatedNodeIds = listOf("reserve_cancel", "payments_how_sena"),
                keywords = setOf("que pasa si cancelo una reserva", "qué pasa si cancelo una reserva", "cancelar reserva con seña", "cancelar turno con seña"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "payments_mocked",
                title = "Pagos y confirmación de reservas",
                message = "En Agendify, el flujo de pagos y señas está pensado para confirmar reservas de forma simple y clara. El pago impacta en el estado del turno y en la agenda del especialista, para que cada reserva quede correctamente registrada.",
                relatedNodeIds = listOf("payments_how_sena", "payments_pending"),
                keywords = setOf("pagos mockeados", "pagos simulados", "demo de pagos", "simulado"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "roles_menu",
                title = "Roles y permisos",
                message = "Estas son las consultas guiadas para entender qué puede hacer cada rol dentro de Agendify.",
                children = listOf(
                    "roles_client",
                    "roles_professional",
                    "roles_assistant",
                    "roles_admin",
                    "roles_choose"
                ),
                keywords = setOf("roles", "permisos", "que rol", "qué rol", "que puede hacer cada rol")
            ),
            ChatbotMenuNode(
                id = "roles_client",
                title = "Qué puede hacer un cliente",
                message = "Un cliente puede buscar especialistas, revisar disponibilidad, reservar turnos, ver sus reservas y cancelarlas cuando corresponda. También puede recibir notificaciones y completar la confirmación de la reserva mediante seña.",
                relatedNodeIds = listOf("reserve_how_to", "reserve_my_reservations"),
                keywords = setOf("que puede hacer un cliente", "qué puede hacer un cliente", "rol cliente", "cliente"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "roles_professional",
                title = "Qué puede hacer un profesional",
                message = "Un profesional puede configurar su agenda, definir horarios, publicar disponibilidad, recibir reservas y organizar su operación diaria. También puede gestionar estados de turnos y trabajar con notas internas.",
                relatedNodeIds = listOf("specialist_config_agenda", "specialist_received_reservations"),
                keywords = setOf("que puede hacer un profesional", "qué puede hacer un profesional", "rol profesional", "profesional"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "roles_assistant",
                title = "Qué puede hacer un asistente",
                message = "Un asistente ayuda al profesional con la gestión operativa de la agenda. Puede colaborar con turnos, disponibilidad y seguimiento diario dentro de los permisos que tenga asignados.",
                relatedNodeIds = listOf("specialist_received_reservations", "specialist_manage_availability"),
                keywords = setOf("que puede hacer un asistente", "qué puede hacer un asistente", "permisos del asistente", "rol asistente", "asistente"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "roles_admin",
                title = "Qué puede hacer un administrador",
                message = "El administrador supervisa configuraciones generales de la plataforma y tiene un alcance distinto al de cliente, profesional o asistente. Su función está enfocada en la gestión global del sistema.",
                relatedNodeIds = listOf("roles_choose"),
                keywords = setOf("que puede hacer un administrador", "qué puede hacer un administrador", "rol admin", "administrador"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "roles_choose",
                title = "Qué rol elegir al registrarme",
                message = "Si vas a reservar turnos para vos, el rol adecuado es cliente. Si vas a atender y administrar tu agenda, el rol natural es profesional. Si solo colaborás con la agenda de otra persona, el rol más conveniente es asistente.",
                relatedNodeIds = listOf("roles_client", "roles_professional", "roles_assistant", "account_role_selection"),
                keywords = setOf("que rol elegir", "qué rol elegir", "que rol me conviene", "elegir rol", "seleccion de rol"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "features_menu",
                title = "Funcionalidades de Agendify",
                message = "Acá tenés un resumen guiado de qué es Agendify, para qué sirve y cómo ayuda a cada tipo de usuario.",
                children = listOf(
                    "features_what_is",
                    "features_purpose",
                    "features_client_benefits",
                    "features_specialist_benefits",
                    "features_vs_whatsapp"
                ),
                keywords = setOf("funcionalidades", "que es agendify", "qué es agendify", "para que sirve", "para qué sirve", "beneficios")
            ),
            ChatbotMenuNode(
                id = "features_what_is",
                title = "Qué es Agendify",
                message = "Agendify es una plataforma para organizar turnos entre clientes, profesionales y asistentes. Centraliza reservas, disponibilidad, agenda y seguimiento del proceso de atención.",
                relatedNodeIds = listOf("features_purpose", "features_vs_whatsapp"),
                keywords = setOf("que es agendify", "qué es agendify", "que es la plataforma", "agendify"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "features_purpose",
                title = "Para qué sirve",
                message = "Agendify sirve para ordenar la gestión de turnos de una forma más clara que una agenda manual. Permite publicar disponibilidad, reservar, administrar agendas y trabajar con distintos roles en un mismo flujo.",
                relatedNodeIds = listOf("features_client_benefits", "features_specialist_benefits"),
                keywords = setOf("para que sirve", "para qué sirve", "que hace agendify", "qué hace agendify"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "features_client_benefits",
                title = "Beneficios para el cliente",
                message = "Para el cliente, Agendify hace más simple encontrar especialistas, ver disponibilidad, reservar sin tantas idas y vueltas y tener un lugar ordenado para seguir sus reservas.",
                relatedNodeIds = listOf("reserve_how_to", "reserve_my_reservations"),
                keywords = setOf("beneficios para el cliente", "ventajas para el cliente"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "features_specialist_benefits",
                title = "Beneficios para el especialista",
                message = "Para el especialista, Agendify ayuda a ordenar la agenda, publicar horarios, recibir reservas de forma centralizada y apoyarse en asistentes o estados de turnos para trabajar mejor el día a día.",
                relatedNodeIds = listOf("specialist_config_agenda", "roles_assistant"),
                keywords = setOf("beneficios para el especialista", "ventajas para el profesional", "beneficios para el profesional"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "features_vs_whatsapp",
                title = "Diferencia con usar solo WhatsApp o agenda manual",
                message = "A diferencia de manejar todo por mensajes o anotaciones manuales, Agendify ordena disponibilidad, reservas, estados y roles en un mismo lugar. Eso hace más simple el seguimiento de la agenda y reduce errores de coordinación.",
                relatedNodeIds = listOf("features_purpose", "specialist_manage_availability"),
                keywords = setOf("diferencia con whatsapp", "agenda manual", "solo whatsapp", "diferencia con agenda manual"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "account_menu",
                title = "Cuenta e inicio de sesión",
                message = "Estas son las consultas guiadas sobre registro, acceso y selección de rol dentro de Agendify.",
                children = listOf(
                    "account_register",
                    "account_login",
                    "account_google",
                    "account_issues",
                    "account_role_selection"
                ),
                keywords = setOf("cuenta", "inicio de sesion", "inicio de sesión", "login", "registrarme", "acceso")
            ),
            ChatbotMenuNode(
                id = "account_register",
                title = "Cómo registrarme",
                message = "Para empezar a usar Agendify, creá una cuenta desde la pantalla de registro y completá los datos básicos que pida el formulario. Después vas a poder elegir o confirmar el rol con el que querés trabajar.",
                relatedNodeIds = listOf("account_login", "account_role_selection"),
                keywords = setOf("como registrarme", "cómo registrarme", "registrarme", "crear cuenta", "como adquirir agendify"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "account_login",
                title = "Cómo iniciar sesión",
                message = "Para iniciar sesión, ingresá a la pantalla de acceso, completá tu correo y contraseña o usá el acceso con Google si vinculaste tu cuenta con ese método.",
                relatedNodeIds = listOf("account_google", "account_issues"),
                keywords = setOf("como iniciar sesion", "cómo iniciar sesión", "iniciar sesion", "login", "entrar a mi cuenta"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "account_google",
                title = "Login con Google",
                message = "Si tu cuenta tiene habilitado el acceso con Google, podés usar ese botón para ingresar de forma más rápida. Una vez dentro, el sistema puede pedirte definir el rol si todavía no quedó configurado.",
                relatedNodeIds = listOf("account_login", "account_role_selection"),
                keywords = setOf("login con google", "entrar con google", "iniciar sesion con google", "google"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "account_issues",
                title = "Problemas para entrar",
                message = "Si tenés problemas para entrar, revisá primero tu correo, la contraseña y el método de acceso que corresponda a tu cuenta. Si el problema continúa, podés pedir ayuda desde soporte.",
                relatedNodeIds = listOf("account_login", "support_contact"),
                keywords = setOf("problemas para entrar", "no puedo iniciar sesion", "error de login", "problema para entrar"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "account_role_selection",
                title = "Selección de rol",
                message = "La selección de rol define si vas a usar Agendify como cliente, profesional o asistente. Ese paso te ayuda a entrar al flujo correcto según el tipo de uso que necesités.",
                relatedNodeIds = listOf("roles_choose", "account_register"),
                keywords = setOf("seleccion de rol", "selección de rol", "elegir rol", "rol al registrarme", "sin definir"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "support_menu",
                title = "Soporte y ayuda",
                message = "Si tu consulta no encaja en una categoría puntual, acá tenés opciones para orientarte mejor o pedir ayuda.",
                children = listOf(
                    "support_not_understood",
                    "support_help_reservation",
                    "support_help_specialist",
                    "support_contact",
                    "support_out_of_scope"
                ),
                keywords = setOf("soporte", "ayuda", "chatbot no entiende", "contactar soporte")
            ),
            ChatbotMenuNode(
                id = "support_not_understood",
                title = "El chatbot no entiende mi consulta",
                message = "Si el chatbot no entiende tu consulta, probá escribir qué querés hacer con palabras más concretas o elegí una categoría del menú. Por ejemplo: reservar un turno, configurar agenda o problemas para entrar.",
                relatedNodeIds = listOf("reserve_menu", "specialist_menu", "account_menu"),
                keywords = setOf("el chatbot no entiende", "no me entiende", "no entiende mi consulta"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "support_help_reservation",
                title = "Necesito ayuda con una reserva",
                message = "Si necesitás ayuda con una reserva, primero revisá si tu duda está en disponibilidad, estado del turno, cancelación o seña. Si después querés ver el detalle exacto de una reserva, podés hacerlo desde tu cuenta.",
                relatedNodeIds = listOf("reserve_menu", "payments_menu"),
                keywords = setOf("ayuda con una reserva", "necesito ayuda con una reserva", "ayuda con mi turno"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "support_help_specialist",
                title = "Necesito ayuda como especialista",
                message = "Si sos especialista y necesitás ayuda, lo mejor es identificar si la duda está en agenda, disponibilidad, reservas recibidas o estados de turnos. Desde acá puedo orientarte con cualquiera de esos temas.",
                relatedNodeIds = listOf("specialist_menu", "roles_assistant"),
                keywords = setOf("ayuda como especialista", "necesito ayuda como especialista", "ayuda profesional"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "support_contact",
                title = "Contactar soporte",
                message = "Si necesitás escalar un problema, usá el canal de soporte disponible en Agendify y explicá brevemente qué estabas intentando hacer, en qué pantalla estabas y qué resultado obtuviste. Eso ayuda a resolver más rápido.",
                relatedNodeIds = listOf("support_help_reservation", "support_help_specialist"),
                keywords = setOf("contactar soporte", "hablar con soporte", "contacto soporte"),
                humanizableFromText = true
            ),
            ChatbotMenuNode(
                id = "support_out_of_scope",
                title = "Consultas fuera del alcance del chatbot",
                message = "Puedo ayudarte a entender cómo funciona Agendify, cómo reservar turnos, cómo administrar agendas y cómo resolver dudas comunes de uso. Si necesitás asistencia más puntual, podés apoyarte en el canal de soporte.",
                relatedNodeIds = listOf("support_contact", "support_not_understood"),
                keywords = setOf("fuera del alcance", "que no puedes hacer", "qué no puedes hacer", "limites del chatbot"),
                humanizableFromText = true
            )
        )

        return allNodes.associateBy { it.id }
    }

    private fun buildParentMap(nodes: Map<String, ChatbotMenuNode>): Map<String, String> {
        val relations = mutableMapOf<String, String>()
        nodes.values.forEach { node ->
            node.children.forEach { childId ->
                relations[childId] = node.id
            }
        }
        return relations
    }

    private fun String.matchesSensitiveIntent(): Boolean =
        hasAny(
            "prompt", "prompts", "instrucciones internas", "reglas internas", "codigo fuente", "código fuente",
            "codigo", "código", "token", "tokens", "api key", "apikey", "credencial", "credenciales",
            "password", "contraseña", "contrasena", "secreto", "secretos", "base de datos interna"
        )

    private fun String.matchesPersonalDataIntent(): Boolean =
        hasAny(
            "que reservas tengo hoy", "qué reservas tengo hoy", "que turnos tengo hoy", "qué turnos tengo hoy",
            "mis reservas de hoy", "mis turnos de hoy", "quien me reservo manana", "quién me reservó mañana",
            "quien me reservo mañana", "cuantas reservas tengo", "cuántas reservas tengo", "reserva de mañana"
        )

    private fun String.matchesGreetingIntent(): Boolean =
        hasAny("hola", "buenas", "buen día", "buen dia", "buenas tardes", "buenas noches", "ayuda", "menu", "menú", "opciones")

    private fun String.hasAny(vararg values: String): Boolean =
        values.any { matchesKeyword(it) }

    private fun String.matchesKeyword(keyword: String): Boolean {
        val inputTokens = split(" ").filter(String::isNotBlank)
        val keywordTokens = keyword.normalize().split(" ").filter(String::isNotBlank)

        if (keywordTokens.isEmpty()) {
            return false
        }

        return keywordTokens.all { keywordToken ->
            inputTokens.any { inputToken -> inputToken.isCloseTo(keywordToken) }
        }
    }

    private fun String.isCloseTo(other: String): Boolean {
        if (this == other) return true
        if (length >= 3 && other.startsWith(this)) return true
        if (other.length >= 3 && startsWith(other)) return true
        return maxOf(length, other.length) >= 5 && levenshteinDistance(other) <= 1
    }

    private fun String.levenshteinDistance(other: String): Int {
        val dp = Array(length + 1) { IntArray(other.length + 1) }

        for (i in 0..length) dp[i][0] = i
        for (j in 0..other.length) dp[0][j] = j

        for (i in 1..length) {
            for (j in 1..other.length) {
                val cost = if (this[i - 1] == other[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }

        return dp[length][other.length]
    }

    private fun String.normalize(): String {
        val lower = lowercase(Locale.getDefault())
        val noAccents = Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")

        return noAccents
            .replace("\\b(cita|citas|reserva|reservas)\\b".toRegex(), "turno")
            .replace("\\b(sena|senia|anticipo)\\b".toRegex(), "pago")
            .replace("\\b(anular|baja)\\b".toRegex(), "cancelar")
            .replace("\\b(especialista|especialistas)\\b".toRegex(), "profesional")
            .replace("\\b(paciente|usuario final|usuario)\\b".toRegex(), "cliente")
            .replace("[^a-z0-9\\s]".toRegex(), " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}

data class ChatbotMenuNode(
    val id: String,
    val title: String,
    val message: String,
    val children: List<String> = emptyList(),
    val relatedNodeIds: List<String> = emptyList(),
    val keywords: Set<String> = emptySet(),
    val humanizableFromText: Boolean = false
)

data class ChatbotReplyDraft(
    val message: String,
    val source: String,
    val options: List<ChatbotOption> = emptyList(),
    val currentNodeId: String? = null,
    val previousNodeId: String? = null,
    val suggestedActions: List<ChatbotOption> = emptyList(),
    val allowHumanization: Boolean = false,
    val factualGuide: String = message
)
