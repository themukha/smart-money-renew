package com.themukha.smartmoney.models

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import java.math.BigDecimal
import java.util.UUID

object Accounts : UUIDTable() {
    val name = varchar("name", 50)
    val walletId = reference("wallet_id", Wallets).index()
    val ownerId = reference("owner_id", Users).index()
    val debit = decimal("debit", 20, 3).default(BigDecimal.ZERO)
    val credit = decimal("credit", 20, 3).default(BigDecimal.ZERO)
    val balance = decimal("balance", 20, 3).default(BigDecimal.ZERO)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}

class Account(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Account>(Accounts)

    var name by Accounts.name
    var walletId by Wallet referencedOn Accounts.walletId
    var ownerId by User referencedOn Accounts.ownerId
    var debit by Accounts.debit
    var credit by Accounts.credit
    var balance by Accounts.balance
    var createdAt by Accounts.createdAt
    val transactions by Transaction referrersOn Transactions.accountId

}