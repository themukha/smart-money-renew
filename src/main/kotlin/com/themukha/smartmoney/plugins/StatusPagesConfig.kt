package com.themukha.smartmoney.plugins

import com.themukha.smartmoney.dto.ErrorResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.util.logging.error

class StatusPagesConfig {
    fun configureStatusPages(application: Application) {
        application.install(StatusPages) {
            exception<Throwable> { call, cause ->
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal Server Error: ${cause.message}"))
                call.application.environment.log.error(cause)
            }
            exception<NotFoundException> { call, cause ->
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Not Found"))
                call.application.environment.log.error(cause)
            }
            exception<IllegalArgumentException> { call, cause ->
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid input: ${cause.message}"))
            }
            exception<NumberFormatException> { call, cause ->
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid number format: ${cause.message}"))
            }

            status(HttpStatusCode.NotFound) { call, status ->
                call.respond(status, ErrorResponse("Not Found"))
            }
            status(HttpStatusCode.BadRequest) { call, status ->
                call.respond(status, ErrorResponse("Bad Request"))
                call.application.environment.log.error("Bad Request:\nRequest:\n${call.request}\nResponse:\n${call.response}")
            }
            status(HttpStatusCode.Unauthorized) { call, status ->
                call.respond(status, ErrorResponse("Unauthorized"))
            }
        }
    }
}