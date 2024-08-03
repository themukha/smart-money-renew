package com.themukha.smartmoney.repositories

import com.themukha.smartmoney.models.User
import com.themukha.smartmoney.models.UserRole
import com.themukha.smartmoney.models.Wallet
import com.themukha.smartmoney.models.WalletUsers
import com.themukha.smartmoney.models.Wallets
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

interface WalletRepository {
    suspend fun createWallet(name: String, currencyCode: String, creator: User): Wallet
    suspend fun getWalletsForUser(userId: UUID): List<Wallet>
    suspend fun getWalletById(walletId: UUID): Wallet?
    suspend fun deleteWallet(walletId: UUID): Boolean
}

class WalletRepositoryImpl : WalletRepository {
    override suspend fun createWallet(name: String, currencyCode: String, creator: User): Wallet = transaction {
        val newWallet = Wallet.new {
            this.name = name
            this.currencyCode = currencyCode
            this.creator = creator
        }

        WalletUsers.insert {
            it[userId] = creator.id
            it[walletId] = newWallet.id
            it[role] = UserRole.ADMIN
        }

        newWallet
    }

    override suspend fun getWalletsForUser(userId: UUID): List<Wallet> = transaction {
        val userWalletIds = WalletUsers
            .slice(WalletUsers.walletId)
            .select { WalletUsers.userId eq userId }
            .map { it[WalletUsers.walletId].value }
        Wallet.find { Wallets.id inList userWalletIds }.toList()
    }

    override suspend fun getWalletById(walletId: UUID): Wallet? = transaction {
        Wallet.findById(walletId)
    }

    override suspend fun deleteWallet(walletId: UUID): Boolean = transaction {
        WalletUsers.deleteWhere { WalletUsers.walletId eq walletId }

        Wallets.deleteWhere { id eq walletId } > 0
    }
}