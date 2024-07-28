package com.themukha.smartmoney.models

import org.jetbrains.exposed.sql.Table

object WalletUsers : Table() {
    val userId = reference("user_id", Users)
    val walletId = reference("wallet_id", Wallets)
    val role = enumeration("role", UserRole::class)

    override val primaryKey = PrimaryKey(userId, walletId, name = "pk_wallet_users")
}

enum class UserRole {
    ADMIN, WRITER, READER
}