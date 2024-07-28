package com.themukha.smartmoney

import com.themukha.smartmoney.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        val routingConfig = RoutingConfig()
        application {
            routingConfig.configureRouting(this)
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Welcome to Smart Money API!", bodyAsText())
        }
    }
}
