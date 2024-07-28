package com.themukha.smartmoney.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

class RoutingConfig {

    fun configureRouting(application: Application) {
        application.routing {
            get("/") {
                call.respondText("Welcome to Smart Money API!")
            }

//            userRoutes()
//            walletRoutes()
//            accountRoutes()
//            transactionRoutes()
//            categoryRoutes()
        }
    }
}