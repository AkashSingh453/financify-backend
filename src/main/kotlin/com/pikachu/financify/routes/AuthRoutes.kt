package com.pikachu.financify.routes

import com.pikachu.financify.auth.GoogleTokenVerifier
import com.pikachu.financify.auth.JwtConfig
import com.pikachu.financify.auth.PasswordHasher
import com.pikachu.financify.db.tables.UsersTable
import com.pikachu.financify.models.*
import com.pikachu.financify.repository.UserRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(userRepository: UserRepository, googleClientId: String) {

    // POST /api/auth/register — Email + Password registration
    post("/api/auth/register") {
        val request = call.receive<RegisterRequest>()
        print(request.email)
        if (request.email.isBlank() || request.password.isBlank() || request.name.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, "All fields are required")
            return@post
        }
        if (request.password.length < 6) {
            call.respond(HttpStatusCode.BadRequest, "Password must be at least 6 characters")
            return@post
        }
        if (!request.email.contains("@")) {
            call.respond(HttpStatusCode.BadRequest, "Invalid email format")
            return@post
        }

        val existingUser = userRepository.findByEmail(request.email)
        if (existingUser != null) {
            call.respond(HttpStatusCode.Conflict, "Email already registered")
            return@post
        }

        val hashedPassword = PasswordHasher.hash(request.password)
        val user = userRepository.createEmailUser(request.email, hashedPassword, request.name)
        val token = JwtConfig.generateToken(user.id, user.email)

        call.respond(HttpStatusCode.Created, AuthResponse(
            token = token, user = user, message = "Registration successful"
        ))
    }

    // POST /api/auth/login — Email + Password login
    post("/api/auth/login") {
        val request = call.receive<LoginRequest>()

        if (request.email.isBlank() || request.password.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Email and password are required"))
            return@post
        }

        val userRow = userRepository.findByEmail(request.email)
        if (userRow == null) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid email or password"))
            return@post
        }

        val storedHash = userRow[UsersTable.passwordHash]
        if (storedHash == null) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(
                message = "This account uses Google sign-in. Please log in with Google."
            ))
            return@post
        }

        if (!PasswordHasher.verify(request.password, storedHash)) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid email or password"))
            return@post
        }

        val userId = userRow[UsersTable.id]
        val email = userRow[UsersTable.email]
        val user = userRepository.findById(userId)!!
        val token = JwtConfig.generateToken(userId, email)

        call.respond(HttpStatusCode.OK, AuthResponse(
            token = token, user = user, message = "Login successful"
        ))
    }

    // POST /api/auth/google — Google OAuth sign-in
    post("/api/auth/google") {
        val request = call.receive<GoogleAuthRequest>()

        if (request.idToken.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Google ID token is required"))
            return@post
        }

        val tokenInfo = GoogleTokenVerifier.verify(request.idToken, googleClientId)
        if (tokenInfo == null) {
            call.respond(HttpStatusCode.Unauthorized, ErrorResponse(message = "Invalid Google token"))
            return@post
        }

        val googleId = tokenInfo.sub
        val email = tokenInfo.email
        val name = tokenInfo.name.ifBlank { "${tokenInfo.givenName} ${tokenInfo.familyName}".trim() }
        val pictureUrl = tokenInfo.picture.ifBlank { null }

        val userRow = userRepository.findByGoogleId(googleId)

        val user: UserResponse
        if (userRow != null) {
            val userId = userRow[UsersTable.id]
            user = userRepository.findById(userId)!!
        } else {
            val existingEmailUser = userRepository.findByEmail(email)
            if (existingEmailUser != null) {
                val userId = existingEmailUser[UsersTable.id]
                userRepository.linkGoogleAccount(userId, googleId, pictureUrl)
                user = userRepository.findById(userId)!!
            } else {
                user = userRepository.createGoogleUser(email, name, googleId, pictureUrl)
            }
        }

        val token = JwtConfig.generateToken(user.id, user.email)
        call.respond(HttpStatusCode.OK, AuthResponse(
            token = token, user = user, message = "Google sign-in successful"
        ))
    }

    // Protected: GET /api/auth/me
    authenticate("auth-jwt") {
        get("/api/auth/me") {
            val principal = call.principal<JWTPrincipal>()!!
            val userId = principal.payload.getClaim("userId").asLong()

            val user = userRepository.findById(userId)
            if (user == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "User not found"))
                return@get
            }

            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = user))
        }
    }
}
