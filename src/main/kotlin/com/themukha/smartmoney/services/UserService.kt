package com.themukha.smartmoney.services

import com.themukha.smartmoney.models.User
import com.themukha.smartmoney.repositories.UserRepository
import java.util.UUID

interface UserService {
    suspend fun findUserByEmail(email: String): User?
    suspend fun findUserById(id: UUID): User?
}

class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    override suspend fun findUserByEmail(email: String): User? {
        return userRepository.findUserByEmail(email)
    }

    override suspend fun findUserById(id: UUID): User? {
        return userRepository.findUserById(id)
    }
}