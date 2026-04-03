package com.pikachu.financify.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object TransactionsTable : Table("transactions") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(UsersTable.id)
    val amount = double("amount")
    val type = varchar("type", 20) // INCOME, EXPENSE
    val category = varchar("category", 50)
    val date = date("date")
    val note = varchar("note", 500).default("")
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)
}
