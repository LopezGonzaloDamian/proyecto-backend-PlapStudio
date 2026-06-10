package plapstudio.agendify.auth

import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import plapstudio.agendify.errors.BusinessException
import plapstudio.agendify.errors.UnauthorizedException
import java.time.Instant

@Service
class GoogleTokenVerifier(
    builder: RestClient.Builder,
    @Value("\${agendify.auth.google.enabled:true}") private val enabled: Boolean,
    @Value("\${agendify.auth.google.client-id:}") private val clientId: String,
    @Value("\${agendify.auth.google.token-info-base-url:https://oauth2.googleapis.com}") private val baseUrl: String
) {
    private val client = builder.baseUrl(baseUrl).build()

    fun verify(idToken: String): GoogleIdentity {
        if (!enabled) throw BusinessException("El ingreso con Google no esta habilitado")
        if (clientId.isBlank()) throw BusinessException("Falta configurar el client id de Google")

        val info = try {
            client.get()
                .uri { builder -> builder.path("/tokeninfo").queryParam("id_token", idToken).build() }
                .retrieve()
                .onStatus(HttpStatusCode::isError) { _, response ->
                    throw UnauthorizedException("No se pudo validar la cuenta de Google")
                }
                .body(GoogleTokenInfo::class.java)
        } catch (_: Exception) {
            throw UnauthorizedException("No se pudo validar la cuenta de Google")
        } ?: throw UnauthorizedException("No se pudo validar la cuenta de Google")

        if (info.aud != clientId) throw UnauthorizedException("El token de Google no corresponde a esta aplicacion")
        if (info.iss != "accounts.google.com" && info.iss != "https://accounts.google.com") {
            throw UnauthorizedException("Origen de token invalido")
        }
        if (info.email.isBlank() || info.sub.isBlank()) throw UnauthorizedException("La cuenta de Google no tiene datos suficientes")
        if (info.emailVerified != null && !info.emailVerified.equals("true", ignoreCase = true)) {
            throw UnauthorizedException("La cuenta de Google debe tener el email verificado")
        }
        if (info.exp.toLongOrNull()?.let { it <= Instant.now().epochSecond } == true) {
            throw UnauthorizedException("El token de Google ya expiro")
        }

        return GoogleIdentity(
            sub = info.sub,
            email = info.email.lowercase(),
            nombreCompleto = info.name?.trim().takeUnless { it.isNullOrBlank() }
                ?: info.givenName?.trim().takeUnless { it.isNullOrBlank() }
                ?: info.email.substringBefore('@')
        )
    }
}

data class GoogleIdentity(
    val sub: String,
    val email: String,
    val nombreCompleto: String
)

private data class GoogleTokenInfo(
    val sub: String = "",
    val email: String = "",
    val aud: String = "",
    val iss: String = "",
    val exp: String = "0",
    val name: String? = null,
    @JsonProperty("given_name") val givenName: String? = null,
    @JsonProperty("email_verified") val emailVerified: String? = null
)
