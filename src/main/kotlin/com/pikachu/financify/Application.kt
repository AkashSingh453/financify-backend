package com.pikachu.financify

import com.pikachu.financify.db.DatabaseFactory
import com.pikachu.financify.plugins.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // Initialize database connection
    DatabaseFactory.init(environment.config)

    // Install CORS for mobile app + web
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        anyHost() // In production, restrict to your domain
    }

    // Default headers
    install(DefaultHeaders) {
        header("X-Engine", "Ktor")
    }

    // Configure plugins
    configureSerialization()
    configureStatusPages()
    configureSecurity()
    configureRouting()

    log.info("🚀 Financify API server started on port ${environment.config.property("ktor.deployment.port").getString()}")
}
