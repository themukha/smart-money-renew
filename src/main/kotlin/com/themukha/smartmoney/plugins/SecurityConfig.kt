package com.themukha.smartmoney.plugins

import com.themukha.smartmoney.auth.JwtService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

class SecurityConfig(private val jwtService: JwtService) {

    fun configureSecurity(application: Application) {

        application.install(Authentication) {
            jwt("auth-jwt") {
                realm = jwtService.realm
                verifier(jwtService.verifier)
                validate { credential ->
                    if (credential.payload.audience.contains(jwtService.audience)) {
                        JWTPrincipal(credential.payload)
                    } else {
                        null
                    }
                }
            }
        }
    }
}