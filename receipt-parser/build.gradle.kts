plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.kotlinSerialization)
    application
}

group = "se.ac.kmp_playground"
version = "1.0-SNAPSHOT"

dependencies {
    // Tabula for PDF table extraction
    implementation("technology.tabula:tabula:1.0.5")

    // JSON serialization - use version from catalog
    implementation(libs.kotlinx.serialization.json)

    // HTTP client for posting to price-api - use versions from catalog
    implementation(libs.ktor.client.core)
    implementation("io.ktor:ktor-client-cio:${libs.versions.ktor.get()}")
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // CLI argument parsing
    implementation("com.github.ajalt.clikt:clikt:5.0.2")

    testImplementation(libs.kotlin.test)
}

application {
    mainClass.set("se.ac.kmp_playground.receipt.MainKt")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}