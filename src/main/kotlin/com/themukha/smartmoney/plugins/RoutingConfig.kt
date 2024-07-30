package com.themukha.smartmoney.plugins

import com.themukha.smartmoney.auth.JwtService
import com.themukha.smartmoney.repositories.UserRepository
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.themukha.smartmoney.auth.Authentication.authenticationRoutes
import org.mindrot.jbcrypt.BCrypt

class RoutingConfig : KoinComponent {

    private val userRepository by inject<UserRepository>()
    private val jwtService by inject<JwtService>()
    private val hashFunction = { it: String -> BCrypt.hashpw(it, BCrypt.gensalt()) }

    fun configureRouting(application: Application) {
        application.routing {
            get("/") {
                call.respondText("Welcome to Smart Money API!")
            }

            authenticationRoutes(userRepository, jwtService, hashFunction)

//            userRoutes()
//            walletRoutes()
//            accountRoutes()
//            transactionRoutes()
//            categoryRoutes()
        }
    }
}