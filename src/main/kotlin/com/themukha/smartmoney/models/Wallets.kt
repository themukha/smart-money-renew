package com.themukha.smartmoney.models

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime
import java.util.UUID

object Wallets : UUIDTable() {
    val name: Column<String> = varchar("name", 50)
    val currencyCode = varchar("currencyCode", 3)
    val createdAt: Column<LocalDateTime> = datetime("createdAt").defaultExpression(CurrentDateTime)
    val creator = reference("creator_id", Users)
    val isActive = bool("isActive").default(true)
}

class Wallet(walletId: EntityID<UUID>) : UUIDEntity(walletId) {
    companion object : UUIDEntityClass<Wallet>(Wallets)

    var name: String by Wallets.name
    var currencyCode: String by Wallets.currencyCode
    var createdAt: LocalDateTime by Wallets.createdAt
    var creator: User by User referencedOn Wallets.creator
    var users by User via WalletUsers
    var isActive by Wallets.isActive
}