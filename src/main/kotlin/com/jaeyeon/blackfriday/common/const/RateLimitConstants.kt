package com.jaeyeon.blackfriday.common.const

object RateLimitConstants {
    private const val NAMESPACE = "blackfriday"
    private const val RATE_PREFIX = "$NAMESPACE:rate"

    object Keys {
        private const val ORDER_RATE_PREFIX = "$RATE_PREFIX:order"
        fun orderRateKey(userId: String): String = "$ORDER_RATE_PREFIX:$userId"
    }
}
