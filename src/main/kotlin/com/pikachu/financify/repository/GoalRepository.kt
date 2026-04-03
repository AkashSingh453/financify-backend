package com.pikachu.financify.repository

import com.pikachu.financify.db.DatabaseFactory.dbQuery
import com.pikachu.financify.db.tables.GoalsTable
import com.pikachu.financify.models.GoalRequest
import com.pikachu.financify.models.GoalResponse
import com.pikachu.financify.models.GoalUpdateRequest
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDate
import java.time.LocalDateTime

class GoalRepository {

    private fun resultRowToResponse(row: ResultRow): GoalResponse {
        return GoalResponse(
            id = row[GoalsTable.id],
            userId = row[GoalsTable.userId],
            title = row[GoalsTable.title],
            targetAmount = row[GoalsTable.targetAmount],
            currentAmount = row[GoalsTable.currentAmount],
            deadline = row[GoalsTable.deadline].toString(),
            category = row[GoalsTable.category],
            isActive = row[GoalsTable.isActive],
            isNoSpendChallenge = row[GoalsTable.isNoSpendChallenge],
            streakDays = row[GoalsTable.streakDays],
            createdAt = row[GoalsTable.createdAt].toString()
        )
    }

    suspend fun getAllByUser(userId: Long): List<GoalResponse> = dbQuery {
        GoalsTable.selectAll()
            .where { GoalsTable.userId eq userId }
            .orderBy(GoalsTable.createdAt, SortOrder.DESC)
            .map { resultRowToResponse(it) }
    }

    suspend fun getActiveByUser(userId: Long): List<GoalResponse> = dbQuery {
        GoalsTable.selectAll()
            .where { (GoalsTable.userId eq userId) and (GoalsTable.isActive eq true) }
            .orderBy(GoalsTable.deadline, SortOrder.ASC)
            .map { resultRowToResponse(it) }
    }

    suspend fun getById(id: Long, userId: Long): GoalResponse? = dbQuery {
        GoalsTable.selectAll()
            .where { (GoalsTable.id eq id) and (GoalsTable.userId eq userId) }
            .map { resultRowToResponse(it) }
            .singleOrNull()
    }

    suspend fun create(userId: Long, request: GoalRequest): GoalResponse = dbQuery {
        val insertStatement = GoalsTable.insert {
            it[GoalsTable.userId] = userId
            it[title] = request.title
            it[targetAmount] = request.targetAmount
            it[currentAmount] = request.currentAmount
            it[deadline] = LocalDate.parse(request.deadline)
            it[category] = request.category
            it[isNoSpendChallenge] = request.isNoSpendChallenge
            it[createdAt] = LocalDateTime.now()
            it[updatedAt] = LocalDateTime.now()
        }
        resultRowToResponse(insertStatement.resultedValues!!.first())
    }

    suspend fun update(id: Long, userId: Long, request: GoalUpdateRequest): Boolean = dbQuery {
        GoalsTable.update({
            (GoalsTable.id eq id) and (GoalsTable.userId eq userId)
        }) {
            request.title?.let { v -> it[title] = v }
            request.targetAmount?.let { v -> it[targetAmount] = v }
            request.currentAmount?.let { v -> it[currentAmount] = v }
            request.deadline?.let { v -> it[deadline] = LocalDate.parse(v) }

            request.category?.let { v -> it[category] = v }
            request.isActive?.let { v -> it[isActive] = v }
            request.streakDays?.let { v -> it[streakDays] = v }
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    suspend fun addSavings(id: Long, userId: Long, amount: Double): Boolean = dbQuery {
        val goal = GoalsTable.selectAll()
            .where { (GoalsTable.id eq id) and (GoalsTable.userId eq userId) }
            .singleOrNull() ?: return@dbQuery false

        val newAmount = goal[GoalsTable.currentAmount] + amount
        val isCompleted = newAmount >= goal[GoalsTable.targetAmount]

        GoalsTable.update({
            (GoalsTable.id eq id) and (GoalsTable.userId eq userId)
        }) {
            it[currentAmount] = newAmount
            if (isCompleted) it[isActive] = false
            it[updatedAt] = LocalDateTime.now()
        } > 0
    }

    suspend fun delete(id: Long, userId: Long): Boolean = dbQuery {
        GoalsTable.deleteWhere {
            (GoalsTable.id eq id) and (GoalsTable.userId eq userId)
        } > 0
    }
}
