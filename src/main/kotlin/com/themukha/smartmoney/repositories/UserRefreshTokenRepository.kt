package com.themukha.smartmoney.repositories

import com.themukha.smartmoney.models.UsersRefreshTokens
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDateTime
import java.util.UUID

interface UserRefreshTokenRepository {
    suspend fun saveRefreshToken(userId: UUID, refreshToken: String, expiresAt: LocalDateTime)
    suspend fun getRefreshTokenForUser(userId: UUID): String?
    suspend fun deleteRefreshTokenForUser(userId: UUID)
    suspend fun deleteExpiredRefreshTokens()
}

class UserRefreshTokenRepositoryImpl : UserRefreshTokenRepository {

    override suspend fun saveRefreshToken(userId: UUID, refreshToken: String, expiresAt: LocalDateTime): Unit = newSuspendedTransaction {
        UsersRefreshTokens.insert {
            it[UsersRefreshTokens.userId] = userId
            it[UsersRefreshTokens.token] = refreshToken
            it[UsersRefreshTokens.expiresAt] = expiresAt
            it[UsersRefreshTokens.createdAt] = LocalDateTime.now()
        }
    }

    override suspend fun getRefreshTokenForUser(userId: UUID): String? = newSuspendedTransaction {
        UsersRefreshTokens.selectAll().where { UsersRefreshTokens.userId eq userId }
            .firstNotNullOfOrNull { it[UsersRefreshTokens.token] }
    }

    override suspend fun deleteRefreshTokenForUser(userId: UUID): Unit = newSuspendedTransaction {
        UsersRefreshTokens.deleteWhere { UsersRefreshTokens.userId eq userId}
    }

    override suspend fun deleteExpiredRefreshTokens(): Unit = newSuspendedTransaction {
        UsersRefreshTokens.deleteWhere { UsersRefreshTokens.expiresAt lessEq LocalDateTime.now() }
    }

}