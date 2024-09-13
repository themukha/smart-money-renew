package com.themukha.smartmoney.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

object UsersRefreshTokens : Table("users_refresh_tokens") {
    val userId = reference("user_id", Users).index()
    val token = varchar("token", 511)
    val expiresAt = datetime("expires_at")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}