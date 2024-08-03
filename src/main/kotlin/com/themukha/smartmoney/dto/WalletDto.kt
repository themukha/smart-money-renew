package com.themukha.smartmoney.dto

import com.themukha.smartmoney.models.UserRole
import com.themukha.smartmoney.models.Users
import com.themukha.smartmoney.models.Wallet
import com.themukha.smartmoney.models.WalletUsers
import com.themukha.smartmoney.utils.LocalDateTimeSerializer
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toKotlinLocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

@Serializable
data class WalletDto(
    @Contextual
    val id: UUID,
    val name: String,
    val currencyCode: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    @Contextual
    val creator: UUID,
    val users: List<WalletUserDto>,
)

@Serializable
data class WalletUserDto(
    @Contextual
    val userId: UUID,
    val name: String,
    val email: String,
    val role: UserRole,
)

fun Wallet.toDto(): WalletDto {
    val usersDto: List<WalletUserDto> = transaction {
        WalletUsers
            .join(Users, JoinType.INNER, WalletUsers.userId, Users.id)
            .select { WalletUsers.walletId eq this@toDto.id }
            .mapNotNull {
                WalletUserDto(
                    userId = it[Users.id].value,
                    name = it[Users.name],
                    email = it[Users.email],
                    role = it[WalletUsers.role]
                )
            }
    }

    return WalletDto(
        id = this.id.value,
        name = this.name,
        currencyCode = this.currencyCode,
        createdAt = this.createdAt.toKotlinLocalDateTime(),
        creator = this.creator.id.value,
        users = usersDto
    )
}