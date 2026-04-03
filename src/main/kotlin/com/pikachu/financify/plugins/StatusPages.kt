package com.pikachu.financify.plugins

import com.pikachu.financify.models.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger("StatusPages")

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            logger.error("Unhandled exception: ${cause.message}", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse(message = "Internal server error: ${cause.localizedMessage}")
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            call.respond(

                HttpStatusCode.BadRequest,
                ErrorResponse(message = cause.localizedMessage ?: "Bad request")
            )
        }

        exception<kotlinx.serialization.SerializationException> { call, cause ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(message = "Invalid request body: ${cause.localizedMessage}")
            )
        }

        status(HttpStatusCode.NotFound) { call, _ ->
            call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(message = "Resource not found")
            )
        }
    }
}
