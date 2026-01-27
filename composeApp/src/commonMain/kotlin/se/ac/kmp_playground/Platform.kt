package se.ac.kmp_playground

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getPlatformTimestamp(): String