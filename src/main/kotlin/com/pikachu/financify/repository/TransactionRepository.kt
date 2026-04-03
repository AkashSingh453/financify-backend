package com.pikachu.financify.repository

import com.pikachu.financify.db.DatabaseFactory.dbQuery
import com.pikachu.financify.db.tables.TransactionsTable
import com.pikachu.financify.models.TransactionRequest
import com.pikachu.financify.models.TransactionResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDate
import java.time.LocalDateTime

class TransactionRepository {


    private fun resultRowToResponse(row: ResultRow): TransactionResponse {
        return TransactionResponse(
            id = row[TransactionsTable.id],
            userId = row[TransactionsTable.userId],
            amount = row[TransactionsTable.amount],
            type = row[TransactionsTable.type],
            category = row[TransactionsTable.category],
            date = row[TransactionsTable.date].toString(),
            note = row[TransactionsTable.note],
            createdAt = row[TransactionsTable.createdAt].toString()
        )
    }

    suspend fun getAllByUser(userId: Long): List<TransactionResponse> = dbQuery {
        TransactionsTable.selectAll()
            .where { TransactionsTable.userId eq userId }
            .orderBy(TransactionsTable.date, SortOrder.DESC)
            .map { resultRowToResponse(it) }
    }

    suspend fun getById(id: Long, userId: Long): TransactionResponse? = dbQuery {
        TransactionsTable.selectAll()
            .where { (TransactionsTable.id eq id) and (TransactionsTable.userId eq userId) }
            .map { resultRowToResponse(it) }
            .singleOrNull()
    }

    suspend fun getByDateRange(
        userId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<TransactionResponse> = dbQuery {
        TransactionsTable.selectAll()
            .where {
                (TransactionsTable.userId eq userId) and
                (TransactionsTable.date greaterEq startDate) and
                (TransactionsTable.date lessEq endDate)
            }
            .orderBy(TransactionsTable.date, SortOrder.DESC)
            .map { resultRowToResponse(it) }
    }

    suspend fun getByType(userId: Long, type: String): List<TransactionResponse> = dbQuery {
        TransactionsTable.selectAll()
            .where { (TransactionsTable.userId eq userId) and (TransactionsTable.type eq type) }
            .orderBy(TransactionsTable.date, SortOrder.DESC)
            .map { resultRowToResponse(it) }
    }

    suspend fun getByCategory(userId: Long, category: String): List<TransactionResponse> = dbQuery {
        TransactionsTable.selectAll()
            .where { (TransactionsTable.userId eq userId) and (TransactionsTable.category eq category) }
            .orderBy(TransactionsTable.date, SortOrder.DESC)
            .map { resultRowToResponse(it) }
    }

    suspend fun search(userId: Long, query: String): List<TransactionResponse> = dbQuery {
        TransactionsTable.selectAll()
            .where {
                (TransactionsTable.userId eq userId) and
                ((TransactionsTable.note like "%$query%") or (TransactionsTable.category like "%$query%"))
            }
            .orderBy(TransactionsTable.date, SortOrder.DESC)
            .map { resultRowToResponse(it) }
    }

    suspend fun create(userId: Long, request: TransactionRequest): TransactionResponse = dbQuery {
        val insertStatement = TransactionsTable.insert {
            it[TransactionsTable.userId] = userId
            it[amount] = request.amount
            it[type] = request.type
            it[category] = request.category
            it[date] = LocalDate.parse(request.date)
            it[note] = request.note
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        resultRowToResponse(insertStatement.resultedValues!!.first())
    }

    suspend fun update(id: Long, userId: Long, request: TransactionRequest): Boolean = dbQuery {
        TransactionsTable.update({
            (TransactionsTable.id eq id) and (TransactionsTable.userId eq userId)
        }) {
            it[amount] = request.amount
            it[type] = request.type
            it[category] = request.category
            it[date] = LocalDate.parse(request.date)
            it[note] = request.note
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    suspend fun delete(id: Long, userId: Long): Boolean = dbQuery {
        TransactionsTable.deleteWhere {
            (TransactionsTable.id eq id) and (TransactionsTable.userId eq userId)
        } > 0
    }

    suspend fun getTotalIncome(userId: Long): Double = dbQuery {
        TransactionsTable.select(TransactionsTable.amount.sum())
            .where { (TransactionsTable.userId eq userId) and (TransactionsTable.type eq "INCOME") }
            .map { it[TransactionsTable.amount.sum()] ?: 0.0 }
            .first()
    }

    suspend fun getTotalExpenses(userId: Long): Double = dbQuery {
        TransactionsTable.select(TransactionsTable.amount.sum())
            .where { (TransactionsTable.userId eq userId) and (TransactionsTable.type eq "EXPENSE") }
            .map { it[TransactionsTable.amount.sum()] ?: 0.0 }
            .first()
    }
}
