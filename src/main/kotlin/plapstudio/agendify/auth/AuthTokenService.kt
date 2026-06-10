package plapstudio.agendify.auth

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Service
class AuthTokenService(
    private val objectMapper: ObjectMapper,
    @Value("\${agendify.auth.token-secret:agendify-dev-secret}") private val secret: String,
    @Value("\${agendify.auth.token-days:30}") private val tokenDays: Long
) {

    fun issueToken(userId: Long): String {
        val now = Instant.now()
        val payload = TokenPayload(
            uid = userId,
            iat = now.epochSecond,
            exp = now.plusSeconds(tokenDays * 24 * 60 * 60).epochSecond
        )
        val encodedPayload = encode(objectMapper.writeValueAsBytes(payload))
        val signature = sign(encodedPayload)
        return "$encodedPayload.$signature"
    }

    fun parseUserId(token: String): Long? {
        val parts = token.split('.')
        if (parts.size != 2) return null
        val payload = parts[0]
        val signature = parts[1]
        if (sign(payload) != signature) return null
        return runCatching {
            val decoded = Base64.getUrlDecoder().decode(payload)
            val tokenPayload = objectMapper.readValue(decoded, TokenPayload::class.java)
            if (tokenPayload.exp <= Instant.now().epochSecond) null else tokenPayload.uid
        }.getOrNull()
    }

    private fun sign(payload: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(secret.toByteArray(StandardCharsets.UTF_8), "HmacSHA256"))
        return encode(mac.doFinal(payload.toByteArray(StandardCharsets.UTF_8)))
    }

    private fun encode(bytes: ByteArray): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)

    private data class TokenPayload(
        val uid: Long = 0,
        val iat: Long = 0,
        val exp: Long = 0
    )
}
