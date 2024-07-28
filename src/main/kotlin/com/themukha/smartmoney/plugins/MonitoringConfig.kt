package com.themukha.smartmoney.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.request.path
import org.slf4j.event.Level

class MonitoringConfig {

    fun configureMonitoring(application: Application) {
        application.install(CallLogging) {
            level = Level.INFO
            filter { call -> call.request.path().startsWith("/") }
        }
    }
}