package com.themukha.smartmoney.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.config.ApplicationConfig

class SecurityConfig(private val config: ApplicationConfig) {

    fun configureSecurity(application: Application) {
        val jwtConfig = config.config("jwt")
        val toPrint = config.config("database")
        println(toPrint.keys())
        val jwtAudience = jwtConfig.property("audience").getString()
        val jwtDomain = jwtConfig.property("domain").getString()
        val jwtRealm = jwtConfig.property("realm").getString()
        val jwtSecret = jwtConfig.property("secret").getString()

        application.install(Authentication) {
            jwt("auth-jwt") {
                realm = jwtRealm
                verifier {
                    JWT
                        .require(Algorithm.HMAC256(jwtSecret))
                        .withAudience(jwtAudience)
                        .withIssuer(jwtDomain)
                        .build()
                }
                validate { credential ->
                    if (credential.payload.audience.contains(jwtAudience)) {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                }
            }
        }
    }
}