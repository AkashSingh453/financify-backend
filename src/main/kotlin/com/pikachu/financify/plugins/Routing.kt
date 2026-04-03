package com.pikachu.financify.plugins

import com.pikachu.financify.repository.GoalRepository
import com.pikachu.financify.repository.TransactionRepository
import com.pikachu.financify.repository.UserRepository
import com.pikachu.financify.routes.authRoutes
import com.pikachu.financify.routes.goalRoutes
import com.pikachu.financify.routes.transactionRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val userRepository = UserRepository()
    val transactionRepository = TransactionRepository()
    val goalRepository = GoalRepository()

    val googleClientId = System.getenv("GOOGLE_CLIENT_ID")

    routing {
        // Health check
        get("/") {
            call.respond(mapOf(
                "status" to "running",
                "app" to "Financify API",
                "version" to "1.0.0"
            ))
        }

        get("/health") {
            call.respond(mapOf("status" to "healthy"))
        }

        // Auth routes (register, login, google)
        authRoutes(userRepository, googleClientId)

        // Data routes (JWT protected)
        transactionRoutes(transactionRepository)
        goalRoutes(goalRepository)
    }
}
