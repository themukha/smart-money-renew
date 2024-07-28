package com.themukha.smartmoney.database

import com.themukha.smartmoney.models.Accounts
import com.themukha.smartmoney.models.Categories
import com.themukha.smartmoney.models.Transactions
import com.themukha.smartmoney.models.Users
import com.themukha.smartmoney.models.WalletUsers
import com.themukha.smartmoney.models.Wallets
import io.ktor.server.config.ApplicationConfig
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {

    fun init(config: ApplicationConfig) {
        val databaseConfig = config.config("database")
        val driverClassName = databaseConfig.property("driver").getString()
        val jdbcUrl = databaseConfig.property("jdbcUrl").getString()
        val username = databaseConfig.property("username").getString()
        val password = databaseConfig.property("password").getString()

        Database.connect(jdbcUrl, driverClassName, username, password)

        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Users,
                Wallets,
                WalletUsers,
                Accounts,
                Transactions,
                Categories
            )
        }
    }
}