package se.ac.kmp_playground

import kotlin.js.Date

class JsPlatform : Platform {
    override val name: String = "Web with Kotlin/JS"
}

actual fun getPlatform(): Platform = JsPlatform()

actual fun getPlatformTimestamp(): String = Date().toISOString()
