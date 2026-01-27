package se.ac.kmp_playground

import platform.UIKit.UIDevice
import platform.Foundation.NSDate
import platform.Foundation.NSISO8601DateFormatter

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getPlatformTimestamp(): String {
    val formatter = NSISO8601DateFormatter()
    return formatter.stringFromDate(NSDate())
}
