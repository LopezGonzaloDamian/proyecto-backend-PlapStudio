package plapstudio.agendify.auth

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class PasswordService(
    private val passwordEncoder: PasswordEncoder
) {
    fun hash(rawPassword: String): String = passwordEncoder.encode(rawPassword)

    fun matches(rawPassword: String, storedPassword: String): Boolean {
        if (storedPassword.isBlank()) return false
        return if (isHash(storedPassword)) {
            passwordEncoder.matches(rawPassword, storedPassword)
        } else {
            storedPassword == rawPassword
        }
    }

    fun shouldUpgrade(storedPassword: String): Boolean =
        storedPassword.isNotBlank() && !isHash(storedPassword)

    private fun isHash(storedPassword: String): Boolean =
        storedPassword.startsWith("\$2a\$") ||
            storedPassword.startsWith("\$2b\$") ||
            storedPassword.startsWith("\$2y\$")
}
