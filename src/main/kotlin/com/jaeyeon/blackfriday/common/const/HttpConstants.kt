package com.jaeyeon.blackfriday.common.const

object HttpConstants {
    object RateLimit {
        const val HEADER_LIMIT = "X-RateLimit-Limit"
        const val HEADER_REMAINING = "X-RateLimit-Remaining"
        const val HEADER_RESET = "X-RateLimit-Reset"
    }
}
