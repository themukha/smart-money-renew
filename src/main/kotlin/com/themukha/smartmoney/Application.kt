package com.themukha.smartmoney

import com.themukha.smartmoney.auth.JwtService
import com.themukha.smartmoney.database.DatabaseFactory
import com.themukha.smartmoney.plugins.*
import com.themukha.smartmoney.repositories.UserRepository
import com.themukha.smartmoney.repositories.UserRepositoryImpl
import com.themukha.smartmoney.repositories.WalletRepository
import com.themukha.smartmoney.repositories.WalletRepositoryImpl
import com.themukha.smartmoney.services.UserService
import com.themukha.smartmoney.services.UserServiceImpl
import com.themukha.smartmoney.services.WalletService
import com.themukha.smartmoney.services.WalletServiceImpl
import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.netty.*
import org.koin.core.context.startKoin
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {

    DatabaseFactory.init(environment.config)

    startKoin {
        modules(appModule(this@module))
    }

    install(Koin) {
        slf4jLogger()
        modules(appModule(this@module))
    }

    configureSecurity()
    configureMonitoring()
    configureSerialization()
    configureRouting()
    configureSwagger()
}

private fun Application.configureSecurity() {
    val securityConfig by inject<SecurityConfig>()
    securityConfig.configureSecurity(this)
}

private fun Application.configureMonitoring() {
    val monitoringConfig by inject<MonitoringConfig>()
    monitoringConfig.configureMonitoring(this)
}

private fun Application.configureSerialization() {
    val serializationConfig by inject<SerializationConfig>()
    serializationConfig.configureSerialization(this)
}

private fun Application.configureRouting() {
    val routingConfig by inject<RoutingConfig>()
    routingConfig.configureRouting(this)
}

private fun Application.configureSwagger() {
    install(SwaggerUI) {
        info {
            title = "Smart Money API"
            version = "0.0.1"
            description = "Smart Money API for testing and demonstration purposes."
        }
        server {
            url = "http://localhost:8080"
            description = "Local development server"
        }
    }
}

private fun applicationConfig(app: Application): ApplicationConfig {
    return app.environment.config
}

fun appModule(app: Application) = org.koin.dsl.module {
    single { applicationConfig(app) }
    single { SerializationConfig() }
    single { JwtService(get()) }
    single { SecurityConfig(get()) }
    single { MonitoringConfig() }
    single { RoutingConfig() }

    single<UserRepository> { UserRepositoryImpl() }
    single<UserService> { UserServiceImpl(get()) }
    single<WalletRepository> { WalletRepositoryImpl() }
    single<WalletService> { WalletServiceImpl(get()) }
}
