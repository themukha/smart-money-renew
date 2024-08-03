package com.themukha.smartmoney.plugins

import com.themukha.smartmoney.utils.LocalDateTimeSerializer
import com.themukha.smartmoney.utils.UUIDSerializer
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.application.Application
import io.ktor.server.application.install
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.util.UUID

class SerializationConfig {

    fun configureSerialization(application: Application) {
        application.install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                serializersModule = SerializersModule {
                    contextual(LocalDateTime::class, LocalDateTimeSerializer)
                    contextual(UUID::class, UUIDSerializer)
                }
            })
        }
    }
}