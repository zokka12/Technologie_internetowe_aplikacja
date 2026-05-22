
plugins {
    id("java-library")
    alias(libs.plugins.jetbrainsKotlinJvm)
    id("application")
    // Nasz nowy tłumacz na format JSON:
    kotlin("plugin.serialization") version "2.0.20"
}
dependencies {
    implementation("io.ktor:ktor-server-core-jvm:3.4.1")
    implementation("io.ktor:ktor-server-netty-jvm:3.4.1")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.4.1")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.4.1")
    implementation("org.jetbrains.exposed:exposed-core:0.59.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.59.0")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("io.ktor:ktor-server-rate-limit-jvm:3.4.1")
    implementation("io.insert-koin:koin-ktor:3.5.3") // Wersja może się delikatnie różnić zależnie od Twoich notatek z wykładu
    implementation("io.insert-koin:koin-logger-slf4j:3.5.3")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

application {
    mainClass.set("pl.zosiaqucz.server.ServerKt")
}