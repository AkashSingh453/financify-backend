package com.pikachu.financify.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

object JwtConfig {

    private lateinit var secret: String
    private lateinit var issuer: String
    private lateinit var audience: String
    lateinit var realm: String
        private set
    private var expirationMs: Long = 86400000 // 24 hours default

    fun init(config: ApplicationConfig) {
        secret = config.property("jwt.secret").getString()
        issuer = config.property("jwt.issuer").getString()
        audience = config.property("jwt.audience").getString()
        realm = config.property("jwt.realm").getString()
        expirationMs = config.property("jwt.expirationMs").getString().toLong()
    }

    private val algorithm: Algorithm
        get() = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier
        get() = JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()

    fun generateToken(userId: Long, email: String): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withExpiresAt(Date(System.currentTimeMillis() + expirationMs))
            .sign(algorithm)
    }

    fun getAudience(): String = audience
    fun getIssuer(): String = issuer
}
