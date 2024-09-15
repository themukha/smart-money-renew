package com.themukha.smartmoney.services

import com.themukha.smartmoney.dto.WalletDto
import com.themukha.smartmoney.dto.toDto
import com.themukha.smartmoney.models.User
import com.themukha.smartmoney.repositories.WalletRepository
import java.util.UUID

interface WalletService {
    suspend fun createWallet(name: String, currencyCode: String, creator: User): WalletDto
    suspend fun findWalletsByUserId(userId: UUID): List<WalletDto>
    suspend fun findWalletById(walletId: UUID, userId: UUID): WalletDto?
    suspend fun deleteWallet(walletId: UUID, userId: UUID): Boolean
}

class WalletServiceImpl(private val walletRepository: WalletRepository) : WalletService {
    override suspend fun createWallet(name: String, currencyCode: String, creator: User): WalletDto {
        return walletRepository.createWallet(name, currencyCode, creator).toDto()
    }

    override suspend fun findWalletsByUserId(userId: UUID): List<WalletDto> {
        return walletRepository.getWalletsForUser(userId).filter { it.isActive }.map { it.toDto() }
    }

    override suspend fun findWalletById(walletId: UUID, userId: UUID): WalletDto? {
        val wallet = walletRepository.findById(walletId)?.takeIf { it.isActive } ?: return null
        return if (wallet.users.any { it.id.value == userId }) {
            wallet.toDto()
        } else null
    }

    override suspend fun deleteWallet(walletId: UUID, userId: UUID): Boolean {
        val wallet = walletRepository.findById(walletId)?.takeIf { it.isActive } ?: return false

        return if (wallet.creator.id.value == userId) {
            walletRepository.deleteWallet(walletId)
        } else {
            false
        }
    }
}