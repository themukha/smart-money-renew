package com.themukha.smartmoney.routes

import com.themukha.smartmoney.auth.getUserIdFromToken
import com.themukha.smartmoney.dto.ErrorResponse
import com.themukha.smartmoney.dto.WalletDto
import com.themukha.smartmoney.models.Wallet
import com.themukha.smartmoney.services.UserService
import com.themukha.smartmoney.services.WalletService
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.delete
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import java.util.UUID

fun Route.walletRoutes() {
    val walletService by inject<WalletService>()
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
                val userId = call.getUserIdFromToken()

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or missing token"))
                    return@post
                }

                val user = userService.findUserById(userId)

                if (user == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("User not found"))
                    return@post
                }

                val createWalletRequest = call.receive<CreateWalletRequest>()
                val wallet = walletService.createWallet(
                    createWalletRequest.name,
                    createWalletRequest.currencyCode,
                    user
                )
                call.respond(HttpStatusCode.Created, wallet)
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
                val userId = call.getUserIdFromToken()

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or missing token"))
                    return@get
                }

                val wallets = walletService.findWalletsByUserId(userId)
                call.respond(HttpStatusCode.OK, wallets)
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
                val userId = call.getUserIdFromToken()

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or missing token"))
                    return@get
                }

                val walletId = call.parameters["walletId"]?.let { UUID.fromString(it) }
                    ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid wallet ID"))

                val wallet = walletService.findWalletById(walletId, userId)

                if (wallet != null) {
                    call.respond(HttpStatusCode.OK, wallet)
                } else {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Wallet not found"))
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
                val userId = call.getUserIdFromToken()

                if (userId == null) {
                    call.respond(HttpStatusCode.Unauthorized, ErrorResponse("Invalid or missing token"))
                    return@delete
                }

                val walletId = call.parameters["walletId"]?.let { UUID.fromString(it) }
                    ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid wallet ID"))

                val success = walletService.deleteWallet(walletId, userId)

                if (success) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.Forbidden, ErrorResponse("User does not have permission to delete this wallet"))
                }
            }
        }
    }
}

@Serializable
data class CreateWalletRequest(val name: String, val currencyCode: String)