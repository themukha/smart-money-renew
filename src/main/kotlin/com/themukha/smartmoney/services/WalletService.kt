package com.themukha.smartmoney.services

import com.themukha.smartmoney.dto.WalletDto
import com.themukha.smartmoney.dto.toDto
import com.themukha.smartmoney.models.User
import com.themukha.smartmoney.repositories.WalletRepository
import java.util.UUID

interface WalletService {
    suspend fun createWallet(name: String, currencyCode: String, creator: User): WalletDto
    suspend fun findWalletsByUserId(userId: UUID): List<WalletDto>
    suspend fun findWalletById(walletId: UUID): WalletDto?
    suspend fun deleteWallet(walletId: UUID): Boolean
}

class WalletServiceImpl(private val walletRepository: WalletRepository) : WalletService {
    override suspend fun createWallet(name: String, currencyCode: String, creator: User): WalletDto {
        return walletRepository.createWallet(name, currencyCode, creator).toDto()
    }

    override suspend fun findWalletsByUserId(userId: UUID): List<WalletDto> {
        return walletRepository.getWalletsForUser(userId).map { it.toDto() }
    }

    override suspend fun findWalletById(walletId: UUID): WalletDto? {
        return walletRepository.getWalletById(walletId)?.toDto()
    }

    override suspend fun deleteWallet(walletId: UUID): Boolean {
        return walletRepository.deleteWallet(walletId)
    }
}