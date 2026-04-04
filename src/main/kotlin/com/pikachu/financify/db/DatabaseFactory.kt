package com.pikachu.financify.db

import com.pikachu.financify.db.tables.GoalsTable
import com.pikachu.financify.db.tables.TransactionsTable
import com.pikachu.financify.db.tables.UsersTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

object DatabaseFactory {

    private val logger = LoggerFactory.getLogger(this::class.java)

    fun init(config: ApplicationConfig) {
        val dbUrl = config.property("database.url").getString()
        val dbUser = config.property("database.user").getString()
        val dbPassword = config.property("database.password").getString()
        val dbDriver = config.property("database.driver").getString()
        val maxPoolSize = config.property("database.maxPoolSize").getString().toInt()

//        val hikariConfig = HikariConfig().apply {
//            jdbcUrl = dbUrl
//            driverClassName = dbDriver
//            username = dbUser
//            password = dbPassword
//            maximumPoolSize = maxPoolSize
//            isAutoCommit = false
//            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
//
//            // Neon PostgreSQL SSL settings
//            addDataSourceProperty("sslmode", "require")
//
//            // --- NEON SERVERLESS OPTIMIZATIONS ---
//            maxLifetime = 300000      // Retire connections after 5 minutes (300,000 ms)
//            keepaliveTime = 60000     // Ping the DB every 1 minute to keep it awake
//            connectionTimeout = 10000 // Timeout fast if Neon is completely asleep
//
//            validate()
//        }

        val hikariConfig = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://postgres_db:5432/my_database"
            driverClassName = "org.postgresql.Driver"
            username = "admin"
            password = "secret"
            isReadOnly = false
            maximumPoolSize = 7
            transactionIsolation = "TRANSACTION_SERIALIZABLE"
        }

        val dataSource = HikariDataSource(hikariConfig)
        Database.connect(dataSource)

        // Create tables if they don't exist
        transaction {
            SchemaUtils.create(
                UsersTable,
                TransactionsTable,
                GoalsTable
            )
        }

        logger.info("✅ Database connected successfully to Neon PostgreSQL")
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
