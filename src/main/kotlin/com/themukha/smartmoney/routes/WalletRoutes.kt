package com.themukha.smartmoney.routes

import com.themukha.smartmoney.dto.WalletDto
import com.themukha.smartmoney.dto.toDto
import com.themukha.smartmoney.models.Wallet
import com.themukha.smartmoney.models.WalletUsers
import com.themukha.smartmoney.repositories.WalletRepository
import com.themukha.smartmoney.services.UserService
import com.themukha.smartmoney.services.WalletService
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.delete
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.walletRoutes() {
    val walletService by inject<WalletService>()
    val walletRepository by inject<WalletRepository>()
    val userService by inject<UserService>()

    route("/wallets") {
        authenticate("auth-jwt") {
            post({
                description = "Create a new wallet"
                request {
                    body<CreateWalletRequest>()
                }
                response {
                    HttpStatusCode.Created to {
                        description = "Wallet was created successfully"
                        body<Wallet>()
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Data validation failed"
                    }
                    HttpStatusCode.Unauthorized to {
                        description = "Invalid or missing token"
                    }
                }
            }) {
                transaction {
                    launch {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.payload?.claims?.get("userId")?.asString()?.let { UUID.fromString(it) }

                        if (userId != null) {
                            val user = userService.findUserById(userId)
                            if (user != null) {
                                val createWalletRequest = call.receive<CreateWalletRequest>()
                                val wallet = walletService.createWallet(
                                    createWalletRequest.name,
                                    createWalletRequest.currencyCode,
                                    user
                                )
                                call.respond(HttpStatusCode.Created, wallet)
                            } else {
                                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                            }
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or missing token"))
                        }
                    }
                }
            }

            get({
                description = "Get all user wallets"
                response {
                    HttpStatusCode.OK to {
                        description = "The list of wallets"
                        body<List<WalletDto>>()
                    }
                    HttpStatusCode.NotFound to {
                        description = "User not found"
                    }
                    HttpStatusCode.Unauthorized to {
                        description = "Invalid or missing token"
                    }
                }
            }) {
                transaction {
                    launch {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.payload?.claims?.get("userId")?.asString()?.let { UUID.fromString(it) }

                        if (userId != null) {
                            val user = userService.findUserById(userId)

                            if (user != null) {
                                val wallets = walletService.findWalletsByUserId(userId)
                                call.respond(HttpStatusCode.OK, wallets)
                            } else {
                                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                            }
                        } else {
                            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or missing token"))
                        }
                    }
                }
            }

            get("/{walletId}", {
                description = "Get wallet details"
                response {
                    HttpStatusCode.OK to {
                        description = "Specific wallet details"
                        body<WalletDto>()
                    }
                    HttpStatusCode.NotFound to {
                        description = "Wallet not found"
                    }
                    HttpStatusCode.Unauthorized to {
                        description = "Invalid or missing token"
                    }
                    HttpStatusCode.Forbidden to {
                        description = "User does not have access to this wallet"
                    }
                }
            }) {
                transaction {
                    launch {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.payload?.claims?.get("userId")?.asString()?.let { UUID.fromString(it) }

                        if (userId == null) {
                            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or missing token"))
                            return@launch
                        }

                        val walletId = call.parameters["walletId"]?.let { UUID.fromString(it) }

                        if (walletId == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid wallet ID"))
                            return@launch
                        }

                        val hasAccess: Boolean = WalletUsers.select { (WalletUsers.walletId eq walletId) and (WalletUsers.userId eq userId) }.any()

                        if (hasAccess) {
                            val wallet = walletRepository.getWalletById(walletId)

                            if (wallet != null) {
                                call.respond(HttpStatusCode.OK, wallet.toDto())
                                return@launch
                            } else {
                                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Wallet not found"))
                                return@launch
                            }
                        } else {
                            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "User does not have access to this wallet"))
                        }
                    }
                }
            }

            delete("/{walletId}", {
                description = "Delete a wallet"
                response {
                    HttpStatusCode.OK to {
                        description = "Wallet was deleted successfully"
                    }
                    HttpStatusCode.NotFound to {
                        description = "Wallet not found"
                    }
                    HttpStatusCode.Forbidden to {
                        description = "User is not the owner of the wallet"
                    }
                    HttpStatusCode.Unauthorized to {
                        description = "Invalid or missing token"
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid wallet ID"
                    }
                }
            }) {
                transaction {
                    launch {
                        val principal = call.principal<JWTPrincipal>()
                        val userId = principal?.payload?.claims?.get("userId")?.asString()?.let { UUID.fromString(it) }

                        if (userId == null) {
                            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or missing token"))
                            return@launch
                        }

                        val walletId = call.parameters["walletId"]?.let { UUID.fromString(it) }

                        if (walletId == null) {
                            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid wallet ID"))
                            return@launch
                        }

                        val wallet = walletRepository.getWalletById(walletId)

                        if (wallet == null) {
                            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Wallet not found"))
                            return@launch
                        }

                        if (wallet.creator.id.value == userId) {
                            val success = walletService.deleteWallet(walletId)
                            if (success) {
                                call.respond(HttpStatusCode.OK)
                            } else {
                                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to delete the wallet"))
                            }
                        } else {
                            call.respond(HttpStatusCode.Forbidden, mapOf("error" to "User is not the owner of the wallet"))
                        }
                    }
                }
            }
        }
    }
}

@Serializable
data class CreateWalletRequest(val name: String, val currencyCode: String)