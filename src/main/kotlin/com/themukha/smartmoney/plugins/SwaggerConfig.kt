package com.themukha.smartmoney.plugins

import io.github.smiley4.ktorswaggerui.SwaggerUI
import io.github.smiley4.ktorswaggerui.data.AuthScheme
import io.github.smiley4.ktorswaggerui.data.AuthType
import io.github.smiley4.ktorswaggerui.data.SwaggerUiSort
import io.github.smiley4.ktorswaggerui.data.SwaggerUiSyntaxHighlight
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig

class SwaggerConfig {

    fun configureSwagger(application: Application, config: ApplicationConfig) {
        application.install(SwaggerUI) {
            info {
                title = "Smart Money API"
                version = "latest"
                description = "Smart Money API for testing and demonstration purposes."
                contact {
                    name = "George Mukha"
                    url = "https://themukha.tech"
                    email = "george@themukha.tech"
                }
            }
            if (config.config("ktor").property("environment").getString() == "prod") {
                server {
                    url = "https://api.smart-money.themukha.tech"
                    description = "Production server"
                }
            } else {
                server {
                    url = "http://localhost:8080"
                    description = "Local development server"
                }
            }
            externalDocs {
                url = "https://github.com/themukha/smart-money-renew"
                description = "GitHub repository"
            }
            swagger {
                displayOperationId = true
                showTagFilterInput = true
                sort = SwaggerUiSort.HTTP_METHOD
                syntaxHighlight = SwaggerUiSyntaxHighlight.IDEA
            }
            security {
                defaultUnauthorizedResponse {
                    description = "Username or password is invalid."
                }
                defaultSecuritySchemeNames = setOf("Basic Auth", "Bearer Token")
                securityScheme("Basic Auth") {
                    type = AuthType.HTTP
                    scheme = AuthScheme.BASIC
                }
                securityScheme("Bearer Token") {
                    type = AuthType.HTTP
                    scheme = AuthScheme.BEARER
                    bearerFormat = "JWT_TOKEN"
                }
            }
            tags {
                tagGenerator = { url -> listOf(url.firstOrNull()) }
            }
        }
    }
}