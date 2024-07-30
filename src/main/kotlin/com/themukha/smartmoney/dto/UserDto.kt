package com.themukha.smartmoney.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val name: String,
    val email: String,
)