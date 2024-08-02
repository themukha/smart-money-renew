package com.themukha.smartmoney.auth

import com.themukha.smartmoney.dto.UserDto
import com.themukha.smartmoney.models.User
import com.themukha.smartmoney.repositories.UserRepository
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.exposedLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.sql.BatchUpdateException
import java.sql.SQLIntegrityConstraintViolationException

object Authentication {

    fun Route.authenticationRoutes(
        db: UserRepository,
        jwtService: JwtService,
        hashFunction: (String) -> String,
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
                                passwordHash = hashFunction(registerRequest.password)
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
                    } ?: call.respond(HttpStatusCode.Conflict, "User with this email already exists")
                } catch (e: Exception) {
                    println(e)
                    call.respond(HttpStatusCode.InternalServerError, "Failed to register user")
                }
            }

            post("/login", {
                description = "Login to the application (get JWT token)"
                request {
                    body<LoginRequest> {
                        description = "Data to login"
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "User successfully logged in"
                        body<TokenResponse> {
                            description = "Token data"
                        }
                    }
                    HttpStatusCode.Unauthorized to {
                        description = "Invalid credentials"
                    }
                }
            }) {
                val loginRequest: LoginRequest = call.receive<LoginRequest>()
                val user: User? = db.findUserByEmail(loginRequest.email)

                if (user != null && BCrypt.checkpw(loginRequest.password, user.passwordHash)) {
                    val token = jwtService.generateToken(user)
                    call.respond(HttpStatusCode.OK, TokenResponse(token = token))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                }
            }
        }
    }
}

@Serializable
data class RegisterRequest(val name: String, val email: String, val password: String)
@Serializable
data class LoginRequest(val email: String, val password: String)
@Serializable
data class TokenResponse(val token: String)