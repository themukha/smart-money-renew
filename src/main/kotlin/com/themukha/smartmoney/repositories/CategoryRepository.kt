package com.themukha.smartmoney.repositories

import com.themukha.smartmoney.models.Categories
import com.themukha.smartmoney.models.Category
import com.themukha.smartmoney.models.CategoryType
import com.themukha.smartmoney.models.Wallet
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.UUID

interface CategoryRepository {
    suspend fun createCategory(name: String, type: CategoryType, isDefault: Boolean, wallet: Wallet?, comment: Map<String, String?>): Category
    suspend fun getCategoriesForWallet(walletId: UUID?): List<Category>
    suspend fun getCategoryById(categoryId: UUID): Category?
    suspend fun updateCategory(category: Category, name: String?, type: CategoryType?, isDefault: Boolean?, comment: Map<String, String?>?): Category?
    suspend fun deleteCategory(categoryId: UUID): Boolean
}

class CategoryRepositoryImpl : CategoryRepository {
    override suspend fun createCategory(
        name: String,
        type: CategoryType,
        isDefault: Boolean,
        wallet: Wallet?,
        comment: Map<String, String?>
    ): Category = newSuspendedTransaction {
        Category.new {
            this.name = name
            this.type = type
            this.isDefault = isDefault
            this.walletId = wallet
        }
    }

    override suspend fun getCategoriesForWallet(
        walletId: UUID?
    ): List<Category> = newSuspendedTransaction {
        val categories = if (walletId != null) {
            Category.find { (Categories.walletId eq walletId) or (Categories.isDefault eq true) }.toList()
        } else {
            Category.find { Categories.isDefault eq true}.toList()
        }
        categories
    }


    override suspend fun getCategoryById(categoryId: UUID): Category? = newSuspendedTransaction {
        Category.findById(categoryId)
    }

    override suspend fun updateCategory(
        category: Category,
        name: String?,
        type: CategoryType?,
        isDefault: Boolean?,
        comment: Map<String, String?>?
    ): Category = newSuspendedTransaction {
        category.apply {
            name?.let { this.name = it }
            type?.let { this.type = it }
            isDefault?.let { this.isDefault = it }
        }
    }

    override suspend fun deleteCategory(categoryId: UUID): Boolean = newSuspendedTransaction {
        Category.findById(categoryId)?.delete() != null
    }
}