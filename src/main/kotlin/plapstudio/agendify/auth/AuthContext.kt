package plapstudio.agendify.auth

import org.springframework.stereotype.Component

@Component
class AuthContext {
    private val holder = ThreadLocal<AuthPrincipal?>()

    fun get(): AuthPrincipal? = holder.get()

    fun set(principal: AuthPrincipal?) {
        holder.set(principal)
    }

    fun clear() {
        holder.remove()
    }
}
