package com.pikachu.financify.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object GoalsTable : Table("goals") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(UsersTable.id)
    val title = varchar("title", 255)
    val targetAmount = double("target_amount")
    val currentAmount = double("current_amount").default(0.0)
    val deadline = date("deadline")
    val category = varchar("category", 50).default("OTHER")
    val isActive = bool("is_active").default(true)
    val isNoSpendChallenge = bool("is_no_spend_challenge").default(false)
    val streakDays = integer("streak_days").default(0)
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
