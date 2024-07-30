package com.themukha.smartmoney.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.themukha.smartmoney.models.User
import io.ktor.server.config.ApplicationConfig
import java.time.Duration
import java.util.Date

class JwtService(config: ApplicationConfig) {

    private val jwtConfig = config.config("jwt")
    private val issuer = jwtConfig.property("issuer").getString()
    val realm = jwtConfig.property("realm").getString()
    val audience = jwtConfig.property("audience").getString()
    private val secret = jwtConfig.property("secret").getString()
    private val validityPeriod = Duration.ofDays(365).toMillis()

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    fun generateToken(user: User): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("userId", user.id.value.toString())
        .withExpiresAt(Date(System.currentTimeMillis() + validityPeriod))
        .sign(algorithm)

}