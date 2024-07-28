package com.themukha.smartmoney.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.application.Application
import io.ktor.server.application.install
import kotlinx.serialization.json.Json

class SerializationConfig {

    fun configureSerialization(application: Application) {
        application.install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }
    }
}