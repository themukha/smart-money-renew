val kotlinVersion: String = "2.0.0"
val logbackVersion: String = "1.5.6"
val postgresVersion: String = "42.7.3"
val h2Version: String = "2.3.230"
val exposedVersion: String = "0.52.0"
val koinKtorVersion: String = "3.5.6"
val mindrotBCryptVersion: String = "0.4"
val ktorSwaggerUiVersion: String = "3.2.0"
val kotlinxJsonVersion: String = "1.7.1"
val kotlinxDatetimeVersion: String = "0.6.0"
val jacksonVersion: String = "2.17.2"

plugins {
    kotlin("jvm") version "2.0.0"
    id("io.ktor.plugin") version "2.3.12"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1" // Для создания fat JAR
    id("application")
    application
}

group = "com.themukha"
version = "0.0.1"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

application {
    mainClass.set("com.themukha.smartmoney.ApplicationKt")
}

tasks {
    register("stage") {
        group = "Application"
        description = "Runs the application"
        dependsOn("clean", "build", "run")
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core")
    implementation("io.ktor:ktor-server-netty")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("io.ktor:ktor-server-webjars-jvm")
    implementation("io.ktor:ktor-server-auth")
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-call-logging")
    implementation("io.ktor:ktor-server-content-negotiation")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.github.smiley4:ktor-swagger-ui:$ktorSwaggerUiVersion")
    implementation("io.insert-koin:koin-ktor:$koinKtorVersion")
    implementation("io.insert-koin:koin-core:$koinKtorVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinKtorVersion")

    // PostgreSQL + other database tools
    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.h2database:h2:$h2Version")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")

    // Other libraries
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("org.mindrot:jbcrypt:$mindrotBCryptVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$kotlinxDatetimeVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxJsonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    testImplementation("io.ktor:ktor-server-tests")
}
