package com.pikachu.financify.routes

import com.pikachu.financify.models.*
import com.pikachu.financify.repository.TransactionRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.*
import java.time.LocalDate

fun Route.transactionRoutes(transactionRepository: TransactionRepository) {

    authenticate("auth-jwt") {

        get("/api/transactions") {
            val userId = getUserId()
            val type = call.request.queryParameters["type"]
            val category = call.request.queryParameters["category"]
            val search = call.request.queryParameters["search"]
            val startDate = call.request.queryParameters["startDate"]

            val endDate = call.request.queryParameters["endDate"]

            val transactions = when {
                !search.isNullOrBlank() -> transactionRepository.search(userId, search)
                !type.isNullOrBlank() -> transactionRepository.getByType(userId, type)
                !category.isNullOrBlank() -> transactionRepository.getByCategory(userId, category)
                !startDate.isNullOrBlank() && !endDate.isNullOrBlank() -> {
                    transactionRepository.getByDateRange(userId, LocalDate.parse(startDate), LocalDate.parse(endDate))
                }
                else -> transactionRepository.getAllByUser(userId)
            }

            call.respond(HttpStatusCode.OK, TransactionListResponse(transactions = transactions, success = true))
        }

        get("/api/transactions/summary") {
            val userId = getUserId()
            val income = transactionRepository.getTotalIncome(userId)
            val expenses = transactionRepository.getTotalExpenses(userId)

            @kotlinx.serialization.Serializable
            data class SummaryData(val totalIncome: Double, val totalExpenses: Double, val balance: Double)

            call.respond(HttpStatusCode.OK, ApiResponse(
                success = true, data = SummaryData(income, expenses, income - expenses)
            ))
        }

        get("/api/transactions/{id}") {
            val userId = getUserId()
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid ID"))

            val transaction = transactionRepository.getById(id, userId)
                ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Transaction not found"))

            call.respond(HttpStatusCode.OK, ApiResponse(success = true, data = transaction))
        }

        post("/api/transactions") {
            val userId = getUserId()
            val request = call.receive<TransactionRequest>()

            if (request.amount <= 0) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Amount must be positive"))
                return@post
            }
            if (request.type !in listOf("INCOME", "EXPENSE")) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Type must be INCOME or EXPENSE"))
                return@post
            }

            val transaction = transactionRepository.create(userId, request)
            call.respond(HttpStatusCode.Created)
        }

        put("/api/transactions/{id}") {
            val userId = getUserId()
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid ID"))

            val request = call.receive<TransactionRequest>()
            val updated = transactionRepository.update(id, userId, request)

            if (updated) {
                val transaction = transactionRepository.getById(id, userId)
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Transaction not found"))
            }
        }

        delete("/api/transactions/{id}") {
            val userId = getUserId()
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse(message = "Invalid ID"))

            val deleted = transactionRepository.delete(id, userId)
            if (deleted) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.NotFound, ErrorResponse(message = "Transaction not found"))
            }
        }
    }
}

// Helper to extract userId from JWT — works inside RoutingContext
private fun PipelineContext<Unit, ApplicationCall>.getUserId(): Long {
    val principal = call.principal<JWTPrincipal>()!!
    return principal.payload.getClaim("userId").asLong()
}
