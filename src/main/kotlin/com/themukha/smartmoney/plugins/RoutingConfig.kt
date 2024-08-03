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
import com.themukha.smartmoney.routes.walletRoutes
import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.ktor.server.application.install
import io.ktor.server.routing.IgnoreTrailingSlash
import io.ktor.server.routing.route
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

            route("/api.json") {
                openApiSpec()
            }

            route("/swagger-ui") {
                swaggerUI("/api.json")
            }

            authenticationRoutes(userRepository, jwtService, hashFunction)
            walletRoutes()

//            userRoutes()
//            accountRoutes()
//            transactionRoutes()
//            categoryRoutes()
        }

        application.install(IgnoreTrailingSlash)
    }
}