package com.themukha.smartmoney

import com.themukha.smartmoney.database.DatabaseFactory
import com.themukha.smartmoney.plugins.*
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val securityConfig = SecurityConfig(environment.config)
    val monitoringConfig = MonitoringConfig()
    val serializationConfig = SerializationConfig()
    val routingConfig = RoutingConfig()

    securityConfig.configureSecurity(this)
    monitoringConfig.configureMonitoring(this)
    serializationConfig.configureSerialization(this)
    routingConfig.configureRouting(this)

    DatabaseFactory.init(environment.config)
}
