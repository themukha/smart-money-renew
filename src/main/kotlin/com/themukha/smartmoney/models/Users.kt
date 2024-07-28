package com.themukha.smartmoney.models

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import java.util.UUID

object Users : UUIDTable() {
    val name: Column<String> = varchar("name", 50).uniqueIndex()
    val email: Column<String> = varchar("email", 255).uniqueIndex()
    val passwordHash: Column<String> = varchar("password_hash", 255)
}

class User(userId: EntityID<UUID>) : UUIDEntity(userId) {
    companion object : UUIDEntityClass<User>(Users)

    var name by Users.name
    var email by Users.email
    var passwordHash by Users.passwordHash
}