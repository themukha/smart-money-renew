package com.themukha.smartmoney.dto

import com.themukha.smartmoney.models.Category
import com.themukha.smartmoney.models.CategoryType
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CategoryDto(
    @Contextual
    val id: UUID,
    val name: String,
    val type: CategoryType,
    val isDefault: Boolean,
    @Contextual
    val walletId: UUID?,
    val comment: Map<String, String?>
)

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val type: CategoryType,
    val isDefault: Boolean = false,
    @Contextual
    val walletId: UUID? = null,
    val comment: Map<String, String?>
)

@Serializable
data class UpdateCategoryRequest(
    val name: String? = null,
    val type: CategoryType? = null,
    val isDefault: Boolean? = null,
    @Contextual
    val walletId: UUID? = null,
    val comment: Map<String, String?>? = null
)


fun Category.toDto(): CategoryDto {
    return CategoryDto(
        id = this.id.value,
        name = this.name,
        type = this.type,
        isDefault = this.isDefault,
        walletId = this.walletId?.id?.value,
        comment = mapOf() // TODO: implement international comments later
    )
}