package com.pikachu.financify.repository

import com.pikachu.financify.db.DatabaseFactory.dbQuery
import com.pikachu.financify.db.tables.UsersTable
import com.pikachu.financify.models.UserResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class UserRepository {

    private fun resultRowToUserResponse(row: ResultRow): UserResponse {
        return UserResponse(
            id = row[UsersTable.id],
            email = row[UsersTable.email],
            name = row[UsersTable.name],
            authProvider = row[UsersTable.authProvider],
            profilePictureUrl = row[UsersTable.profilePictureUrl],
            createdAt = row[UsersTable.createdAt].toString()
        )
    }

    suspend fun findByEmail(email: String): ResultRow? = dbQuery {
        UsersTable.selectAll().where { UsersTable.email eq email }.singleOrNull()
    }

    suspend fun findByGoogleId(googleId: String): ResultRow? = dbQuery {
        UsersTable.selectAll().where { UsersTable.googleId eq googleId }.singleOrNull()
    }

    suspend fun findById(id: Long): UserResponse? = dbQuery {
        UsersTable.selectAll().where { UsersTable.id eq id }
            .map { resultRowToUserResponse(it) }
            .singleOrNull()
    }

    suspend fun createEmailUser(email: String, passwordHash: String, name: String): UserResponse = dbQuery {
        val insertStatement = UsersTable.insert {
            it[UsersTable.email] = email
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.name] = name
            it[UsersTable.authProvider] = "EMAIL"
            it[UsersTable.createdAt] = LocalDateTime.now()
            it[UsersTable.updatedAt] = LocalDateTime.now()
        }
        resultRowToUserResponse(insertStatement.resultedValues!!.first())
    }

    suspend fun createGoogleUser(
        email: String,
        name: String,
        googleId: String,
        profilePictureUrl: String?
    ): UserResponse = dbQuery {
        val insertStatement = UsersTable.insert {
            it[UsersTable.email] = email
            it[UsersTable.name] = name
            it[UsersTable.googleId] = googleId
            it[UsersTable.authProvider] = "GOOGLE"
            it[UsersTable.profilePictureUrl] = profilePictureUrl
            it[UsersTable.createdAt] = LocalDateTime.now()
            it[UsersTable.updatedAt] = LocalDateTime.now()
        }
        resultRowToUserResponse(insertStatement.resultedValues!!.first())
    }

    suspend fun linkGoogleAccount(userId: Long, googleId: String, profilePictureUrl: String?) = dbQuery {
        UsersTable.update({ UsersTable.id eq userId }) {
            it[UsersTable.googleId] = googleId
            it[UsersTable.authProvider] = "BOTH"
            if (profilePictureUrl != null) {
                it[UsersTable.profilePictureUrl] = profilePictureUrl
            }
            it[UsersTable.updatedAt] = LocalDateTime.now()
        }
    }

    suspend fun updateProfile(userId: Long, name: String? = null, profilePictureUrl: String? = null) = dbQuery {
        UsersTable.update({ UsersTable.id eq userId }) {
            if (name != null) it[UsersTable.name] = name
            if (profilePictureUrl != null) it[UsersTable.profilePictureUrl] = profilePictureUrl
            it[UsersTable.updatedAt] = LocalDateTime.now()
        }
    }
}
