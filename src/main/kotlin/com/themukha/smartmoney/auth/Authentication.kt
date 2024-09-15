package com.themukha.smartmoney.auth

import com.auth0.jwt.exceptions.JWTVerificationException
import com.themukha.smartmoney.dto.ErrorResponse
import com.themukha.smartmoney.dto.UserDto
import com.themukha.smartmoney.models.User
import com.themukha.smartmoney.repositories.UserRefreshTokenRepository
import com.themukha.smartmoney.repositories.UserRepository
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.exposedLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.sql.BatchUpdateException
import java.sql.SQLIntegrityConstraintViolationException
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.UUID

object Authentication {

    fun Route.authenticationRoutes(
        userRepository: UserRepository,
        userRefreshTokenRepository: UserRefreshTokenRepository,
        jwtService: JwtService,
        hashFunction: (String, Int) -> String,
    ) {
        route("/auth") {
            post("/register", {
                description = "Register a new user"
                request {
                    body<RegisterRequest> {
                        description = "Data to register a new user"
                    }
                }
                response {
                    HttpStatusCode.Created to {
                        description = "User successfully created"
                        body<UserDto> {
                            description = "User data"
                        }
                    }
                    HttpStatusCode.Conflict to {
                        description = "User with this email already exists"
                    }
                }
            }) {
                val registerRequest: RegisterRequest = call.receive<RegisterRequest>()

                try {
                    val newUser: User? = transaction {
                        try {
                            User.new {
                                name = registerRequest.name
                                email = registerRequest.email
                                passwordHash = hashFunction(registerRequest.password, 12)
                            }
                        } catch (e: Exception) {
                            val originalException = (e as? ExposedSQLException)?.cause
                            when (originalException) {
                                is SQLIntegrityConstraintViolationException ->{
                                    exposedLogger.error("Unique constraint violation: ${e.message}")
                                    null
                                }
                                is BatchUpdateException -> {
                                    exposedLogger.error("Unique constraint violation: ${e.message}")
                                    null
                                }
                                else -> throw e
                            }
                        }
                    }

                    newUser?.let {
                        call.respond(HttpStatusCode.Created, UserDto(name = it.name, email = it.email))
                    } ?: call.respond(HttpStatusCode.Conflict, ErrorResponse("User with this email already exists"))
                } catch (e: Exception) {
                    println(e)
                    call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Failed to register user"))
                }
            }

            authenticate("auth-basic") {
                post("/login", {
                    description = "Login to the application (get JWT token) using Basic Auth"
                    response {
                        HttpStatusCode.OK to {
                            description = "User successfully logged in. Token is set in a cookie"
                        }
                        HttpStatusCode.Unauthorized to {
                            description = "Invalid credentials"
                        }
                    }
                }) {
                    val basicAuthHeader = call.request.headers["Authorization"]

                    if (basicAuthHeader == null || !basicAuthHeader.startsWith("Basic ")) {
                        call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Missing or invalid Basic Auth header"))
                        return@post
                    }

                    val base64Credentials = basicAuthHeader.substringAfter("Basic ")
                    val credentials = String(Base64.getDecoder().decode(base64Credentials)).split(":")

                    if (credentials.size != 2) {
                        call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid Basic Auth credentials format"))
                        return@post
                    }

                    val email = credentials[0]
                    val password = credentials[1]

                    val user: User? = userRepository.findUserByEmail(email)

                    if (user != null && BCrypt.checkpw(password, user.passwordHash)) {
                        val token = jwtService.generateToken(user)
                        val refreshToken = jwtService.generateRefreshToken(user)
                        val expiresAt = LocalDateTime.now().plus(jwtService.refreshTokenValidityPeriod, ChronoUnit.MILLIS)

                        userRefreshTokenRepository.deleteRefreshTokenForUser(user.id.value)
                        userRefreshTokenRepository.saveRefreshToken(user.id.value, refreshToken, expiresAt)

                        call.response.cookies.append(
                            name = "jwtToken",
                            value = token,
                            path = "/",
                            httpOnly = true,
                            maxAge = jwtService.validityPeriod / 1000 // to seconds
                        )

                        call.response.cookies.append(
                            name = "refreshToken",
                            value = refreshToken,
                            path = "/",
                            httpOnly = true,
                            maxAge = jwtService.refreshTokenValidityPeriod / 1000 // to seconds
                        )

                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid credentials"))
                    }
                }
            }

            authenticate("auth-jwt") {
                post("/refresh", {
                    description = "Refresh access token using a refresh token"
                    response {
                        HttpStatusCode.OK to {
                            description = "Access token refreshed successfully. New token is set in cookie."
                        }
                        HttpStatusCode.Unauthorized to {
                            description = "Invalid or missing refresh token"
                        }
                    }
                }) {
                    transaction {
                        launch {
                            val userId = call.getUserIdFromToken()

                            if (userId == null) {
                                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Missing JWT token"))
                                return@launch
                            }


                            val refreshTokenFromCookie = call.request.cookies["refreshToken"]

                            if (refreshTokenFromCookie == null) {
                                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Missing refresh token"))
                                return@launch
                            }

                            try {
                                val decodedRefreshToken = jwtService.verifierRefresh.verify(refreshTokenFromCookie)
                                val tokenId = UUID.fromString(decodedRefreshToken.getClaim("userId").asString())

                                if (tokenId != userId) {
                                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid refresh token"))
                                    return@launch
                                }

                                val storedRefreshToken = userRefreshTokenRepository.getRefreshTokenForUser(userId)

                                if (storedRefreshToken == refreshTokenFromCookie) {
                                    val user = userRepository.findUserById(userId)
                                    if (user == null) {
                                        call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid refresh token"))
                                        return@launch
                                    }
                                    val newAccessToken = jwtService.generateToken(user)

                                    call.response.cookies.append(
                                        name = "jwtToken",
                                        value = newAccessToken,
                                        path = "/",
                                        httpOnly = true,
                                        maxAge = jwtService.validityPeriod / 1000 // to seconds
                                    )
                                    call.respond(HttpStatusCode.OK, "Access token refreshed successfully")
                                }
                            } catch (e: JWTVerificationException) {
                                call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid refresh token"))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String)