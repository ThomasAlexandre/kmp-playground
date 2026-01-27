@file:OptIn(ExperimentalJsExport::class)
@file:Suppress("OPT_IN_USAGE")

package se.ac.kmp_playground

class WasmPlatform : Platform {
    override val name: String = "Web with Kotlin/Wasm"
}

actual fun getPlatform(): Platform = WasmPlatform()

@JsFun("() => new Date().toISOString()")
private external fun jsGetTimestamp(): String

actual fun getPlatformTimestamp(): String = jsGetTimestamp()