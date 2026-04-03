package com.pikachu.financify.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object UsersTable : Table("users") {
    val id = long("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255).nullable()
    val name = varchar("name", 255)
    val googleId = varchar("google_id", 255).nullable().uniqueIndex()
    val authProvider = varchar("auth_provider", 50).default("EMAIL") // EMAIL, GOOGLE, BOTH
    val profilePictureUrl = varchar("profile_picture_url", 500).nullable()
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
