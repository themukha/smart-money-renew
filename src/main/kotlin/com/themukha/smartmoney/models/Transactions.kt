package com.themukha.smartmoney.models

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import java.util.UUID

object Transactions : UUIDTable() {
    val accountId = reference("account_id", Accounts).index()
    val type = enumeration("type", TransactionType::class)
    val amount = decimal("amount", 10, 3)
    val category = reference("category_id", Categories).nullable()
    val dateTime = datetime("date_time")
    val description = text("description").nullable()
}

class Transaction(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Transaction>(Transactions)

    var account by Account referencedOn Transactions.accountId
    var type by Transactions.type
    var amount by Transactions.amount
    var category by Category optionalReferencedOn Transactions.category
    var dateTime by Transactions.dateTime
    var description by Transactions.description
}

enum class TransactionType {
    INCOME,
    EXPENSE
}