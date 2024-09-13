package com.themukha.smartmoney.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.themukha.smartmoney.models.User
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.config.ApplicationConfig
import java.time.Duration
import java.util.Date
import java.util.UUID

class JwtService(config: ApplicationConfig) {

    private val jwtConfig = config.config("jwt")
    private val issuer = jwtConfig.property("issuer").getString()
    val realm = jwtConfig.property("realm").getString()
    val audience = jwtConfig.property("audience").getString()
    val refreshAudience = jwtConfig.property("refreshAudience").getString()
    private val secret = jwtConfig.property("secret").getString()
    private val validityPeriodInDays = jwtConfig.property("tokenValidityDays").getString().toLong()
    private val refreshTokenValidityPeriodInDays = jwtConfig.property("refreshTokenValidityDays").getString().toLong()
    internal val validityPeriod = Duration.ofDays(validityPeriodInDays).toMillis()
    internal val refreshTokenValidityPeriod = Duration.ofDays(refreshTokenValidityPeriodInDays).toMillis()

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(audience)
        .build()

    val verifierRefresh: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .withAudience(refreshAudience)
        .build()

    fun generateToken(user: User): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("userId", user.id.value.toString())
        .withExpiresAt(Date(System.currentTimeMillis() + validityPeriod))
        .sign(algorithm)

    fun generateRefreshToken(user: User): String = JWT.create()
        .withSubject("RefreshToken")
        .withIssuer(issuer)
        .withAudience(refreshAudience)
        .withClaim("userId", user.id.value.toString())
        .withExpiresAt(Date(System.currentTimeMillis() + refreshTokenValidityPeriod))
        .sign(algorithm)
}

fun ApplicationCall.getUserIdFromToken(): UUID? {
    return principal<JWTPrincipal>()?.payload?.claims?.get("userId")?.asString()?.let {
        UUID.fromString(it)
    }
}