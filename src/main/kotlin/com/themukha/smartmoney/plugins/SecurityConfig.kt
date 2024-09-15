package com.themukha.smartmoney.plugins

import com.themukha.smartmoney.auth.JwtService
import com.themukha.smartmoney.models.User
import com.themukha.smartmoney.repositories.UserRepository
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.basic
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.mindrot.jbcrypt.BCrypt
import java.util.Date

class SecurityConfig(private val jwtService: JwtService) : KoinComponent {

    private val userRepository by inject<UserRepository>()

    fun configureSecurity(application: Application) {

        application.install(Authentication) {
            jwt("auth-jwt") {
                realm = jwtService.realm
                verifier(jwtService.verifier)
                validate { credential ->
                    val token = credential.payload
                    val iss = token.issuer
                    val aud = token.audience
                    val expiresAt = token.expiresAt?.let { Date(it.time) }
                    val notBefore = token.notBefore?.let { Date(it.time) }
                    if (
                        aud.equals(jwtService.audience) &&
                        iss.equals(jwtService.issuer) &&
                        expiresAt != null && expiresAt.after(Date()) &&
                        (notBefore == null || notBefore.before(Date()))
                    ) {
                        JWTPrincipal(token)
                    } else {
                        null
                    }
                }
            }

            basic("auth-basic") {
                realm = "Access to the '/auth/login' path"
                validate { credentials ->
                    val email = credentials.name
                    val password = credentials.password

                    val user: User? = userRepository.findUserByEmail(email)

                    if (user != null && BCrypt.checkpw(password, user.passwordHash)) {
                        UserIdPrincipal(user.id.value.toString())
                    } else {
                        null
                    }
                }
            }
        }
    }
}