package com.themukha.smartmoney.repositories

import com.themukha.smartmoney.models.User
import com.themukha.smartmoney.models.Users
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.exposedLogger
import java.sql.BatchUpdateException
import java.sql.SQLIntegrityConstraintViolationException
import java.util.UUID

interface UserRepository {
    suspend fun createUser(user: User): User?
    suspend fun findUserByEmail(email: String): User?
    suspend fun findUserById(userId: UUID): User?
    suspend fun findAll(): List<User>
    suspend fun update(user: User): User?
    suspend fun delete(userId: UUID): Boolean
}
class UserRepositoryImpl : UserRepository {

    override suspend fun createUser(user: User): User? = transaction {
        try {
            User.new {
                name = user.name
                email = user.email
                passwordHash = user.passwordHash
                isActive = true
            }
        } catch (e: Exception) {
            val originalException = (e as? ExposedSQLException)?.cause
            when (originalException) {
                is SQLIntegrityConstraintViolationException ->{
                    exposedLogger.error("Unique constraint violation: ${e.message}")
                    null
                }
                is BatchUpdateException -> {
                    exposedLogger.error("Unique constraint violation: ${e.message}")
                    null
                }
                else -> throw e
            }
        }
    }

    override suspend fun findUserByEmail(email: String): User? = transaction {
        User.find {
            Users.email eq email
        }.firstOrNull()
    }

    override suspend fun findUserById(userId: UUID): User? = transaction {
        User.find {
            Users.id eq userId
        }.firstOrNull()
    }

    override suspend fun findAll(): List<User> = transaction {
        User.all().toList()
    }

    override suspend fun update(user: User) = transaction {
        User.find {
            Users.id eq user.id
        }.firstOrNull()?.apply {
            name = user.name
            email = user.email
            passwordHash = user.passwordHash
            isActive = user.isActive
        }
    }

    override suspend fun delete(userId: UUID): Boolean = transaction {
        User.findById(userId)?.apply {
            this.isActive = false
        } != null
    }
}