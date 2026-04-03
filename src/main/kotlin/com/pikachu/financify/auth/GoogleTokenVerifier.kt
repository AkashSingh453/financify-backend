package com.pikachu.financify.auth

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

/**
 * Verifies Google ID tokens by calling Google's tokeninfo endpoint.
 * 
 * Flow:
 * 1. Android app signs in with Google and gets an ID token
 * 2. App sends the ID token to our backend
 * 3. Backend verifies the token with Google's API
 * 4. If valid, creates/updates user and issues our own JWT
 */
object GoogleTokenVerifier {

    private val logger = LoggerFactory.getLogger(this::class.java)
    private const val GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo"

    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    @Serializable
    data class GoogleTokenInfo(
        val sub: String = "",           // Google user ID
        val email: String = "",
        @SerialName("email_verified")
        val emailVerified: String = "",
        val name: String = "",
        val picture: String = "",
        @SerialName("given_name")
        val givenName: String = "",
        @SerialName("family_name")
        val familyName: String = "",
        val aud: String = "",          // Client ID
        val iss: String = "",          // Issuer
        val exp: String = ""           // Expiration
    )

    /**
     * Verify a Google ID token and return the parsed token info.
     * Returns null if the token is invalid.
     */
    suspend fun verify(idToken: String, expectedClientId: String): GoogleTokenInfo? {
        return try {
            val response: GoogleTokenInfo = httpClient.get(GOOGLE_TOKEN_INFO_URL) {
                parameter("id_token", idToken)
            }.body()

            // Verify the audience matches our client ID
            if (response.aud != expectedClientId) {
                logger.warn("Google token audience mismatch: expected=$expectedClientId, got=${response.aud}")
                return null
            }

            // Verify the email is verified
            if (response.emailVerified != "true") {
                logger.warn("Google email not verified for: ${response.email}")
                return null
            }

            // Verify issuer
            if (response.iss != "accounts.google.com" && response.iss != "https://accounts.google.com") {
                logger.warn("Invalid Google token issuer: ${response.iss}")
                return null
            }

            logger.info("✅ Google token verified for: ${response.email}")
            response
        } catch (e: Exception) {
            logger.error("❌ Google token verification failed: ${e.message}")
            null
        }
    }
}
