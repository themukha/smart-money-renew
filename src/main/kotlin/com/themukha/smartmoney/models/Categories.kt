package com.themukha.smartmoney.models

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import java.util.UUID

object Categories : UUIDTable() {
    val name = varchar("name", 50)
    val walletId = reference("wallet_id", Wallets)
    val type = enumeration("type", CategoryType::class)
}

class Category(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<Category>(Categories)

    var name by Categories.name
    var walletId by Wallet referencedOn Categories.walletId
    var type by Categories.type
}

enum class CategoryType {
    INCOME,
    EXPENSE
}