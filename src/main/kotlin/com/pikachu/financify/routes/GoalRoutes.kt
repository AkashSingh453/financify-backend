package com.pikachu.financify.routes

import com.pikachu.financify.models.*
import com.pikachu.financify.repository.GoalRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*

fun Route.goalRoutes(goalRepository: GoalRepository) {

    authenticate("auth-jwt") {

        get("/api/goals") {
            val userId = getGoalUserId()
            val activeOnly = call.request.queryParameters["active"]?.toBooleanStrictOrNull()

            val goals = if (activeOnly == true) {
                goalRepository.getActiveByUser(userId)
            } else {
                goalRepository.getAllByUser(userId)
            }

            call.respond(HttpStatusCode.OK, GoalListResponse(goals = goals, success = true))
        }

        get("/api/goals/{id}") {
            val userId = getGoalUserId()
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid ID"))

            val goal = goalRepository.getById(id, userId)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Goal not found"))

            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = goal))
        }

        post("/api/goals") {
            val userId = getGoalUserId()
            val request = call.receive<GoalRequest>()

            if (request.title.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Title is required"))
                return@post
            }
            if (request.targetAmount <= 0) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Target amount must be positive"))
                return@post
            }

            val goal = goalRepository.create(userId, request)
            call.respond(HttpStatusCode.Created)
        }

        put("/api/goals/{id}") {
            val userId = getGoalUserId()
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid ID"))

            val request = call.receive<GoalUpdateRequest>()
            val updated = goalRepository.update(id, userId, request)

            if (updated) {
                val goal = goalRepository.getById(id, userId)
                call.respond(HttpStatusCode.OK )
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Goal not found"))
            }
        }

        post("/api/goals/{id}/savings") {
            val userId = getGoalUserId()
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid ID"))

            val request = call.receive<AddSavingsRequest>()
            if (request.amount <= 0) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Amount must be positive"))
                return@post
            }

            val updated = goalRepository.addSavings(id, userId, request.amount)
            if (updated) {
                val goal = goalRepository.getById(id, userId)
                call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = goal, message = "Savings added"))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Goal not found"))
            }
        }

        delete("/api/goals/{id}") {
            val userId = getGoalUserId()
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid ID"))

            val deleted = goalRepository.delete(id, userId)
            if (deleted) {
                call.respond(HttpStatusCode.OK, ApiResponse<Unit>(success = true, message = "Goal deleted"))
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Goal not found"))
            }
        }
    }
}

private fun PipelineContext<Unit, ApplicationCall>.getGoalUserId(): Long {
    val principal = call.principal<JWTPrincipal>()!!
    return principal.payload.getClaim("userId").asLong()
}
