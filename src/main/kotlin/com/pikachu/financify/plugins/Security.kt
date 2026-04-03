package com.pikachu.financify.plugins

import com.pikachu.financify.auth.JwtConfig
import com.pikachu.financify.models.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*

fun Application.configureSecurity() {
    JwtConfig.init(environment.config)

    install(Authentication) {
        jwt("auth-jwt") {
            realm = JwtConfig.realm

            verifier(JwtConfig.verifier)

            validate { credential ->
                if (credential.payload.getClaim("userId").asLong() != null &&
                    credential.payload.audience.contains(JwtConfig.getAudience())
                ) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }

            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(message = "Token is invalid or has expired")
                )
            }
        }
    }
}
