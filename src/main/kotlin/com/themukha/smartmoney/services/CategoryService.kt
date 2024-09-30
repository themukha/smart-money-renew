package com.themukha.smartmoney.services

import com.themukha.smartmoney.dto.CategoryDto
import com.themukha.smartmoney.dto.CreateCategoryRequest
import com.themukha.smartmoney.dto.UpdateCategoryRequest
import com.themukha.smartmoney.dto.toDto
import com.themukha.smartmoney.repositories.CategoryRepository
import com.themukha.smartmoney.repositories.WalletRepository
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

interface CategoryService {
    suspend fun createCategory(request: CreateCategoryRequest, creator: UUID): CategoryDto
    suspend fun getCategoriesForUser(userId: UUID, walletId: UUID?): List<CategoryDto>
    suspend fun getCategoryById(categoryId: UUID): CategoryDto?
    suspend fun updateCategory(categoryId: UUID, request: UpdateCategoryRequest): CategoryDto?
    suspend fun deleteCategory(categoryId: UUID): Boolean
}

class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val walletRepository: WalletRepository
) : CategoryService {

    override suspend fun createCategory(request: CreateCategoryRequest, creator: UUID): CategoryDto = newSuspendedTransaction {
        val wallet = request.walletId?.let { walletRepository.findById(it) }
        val category = categoryRepository.createCategory(request.name, request.type, request.isDefault, wallet, request.comment)

        category.toDto()
    }

    override suspend fun getCategoriesForUser(userId: UUID, walletId: UUID?): List<CategoryDto> = newSuspendedTransaction {
        categoryRepository.getCategoriesForWallet(walletId).map { it.toDto() }
    }

    override suspend fun getCategoryById(categoryId: UUID): CategoryDto? = newSuspendedTransaction {
        categoryRepository.getCategoryById(categoryId)?.toDto()
    }


    override suspend fun updateCategory(categoryId: UUID, request: UpdateCategoryRequest): CategoryDto? = newSuspendedTransaction {
        val existingCategory = categoryRepository.getCategoryById(categoryId) ?: return@newSuspendedTransaction null

        val updatedCategory = categoryRepository.updateCategory(
            existingCategory,
            request.name,
            request.type,
            request.isDefault,
            request.comment
        )
        updatedCategory?.toDto()
    }

    override suspend fun deleteCategory(categoryId: UUID): Boolean = newSuspendedTransaction {
        categoryRepository.deleteCategory(categoryId)
    }
}