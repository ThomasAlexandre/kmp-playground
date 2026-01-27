package se.ac.kmp_playground

import android.os.Build
import java.time.Instant

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getPlatformTimestamp(): String = Instant.now().toString()