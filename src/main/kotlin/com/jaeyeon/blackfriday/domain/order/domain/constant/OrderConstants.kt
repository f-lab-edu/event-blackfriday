package com.jaeyeon.blackfriday.domain.order.domain.constant

object OrderConstants {
    const val MIN_QUANTITY = 1
    const val MIN_PRICE = 0

    object Queue {
        const val PROCESSING_THRESHOLD = 3
        const val LOCK_TIMEOUT_SECONDS = 30
        const val DEFAULT_POSITION_WHEN_NOT_IN_QUEUE = 0L
        const val PROCESSING_COUNT_RESET_VALUE = 0
        const val MILLISECONDS_TO_SECONDS = 1000.0
        const val QUEUE_POSITION_OFFSET = 1
        const val EMPTY_QUEUE_SIZE = 0L

        object Keys {
            private const val NAMESPACE = "blackfriday"
            private const val ORDER_PREFIX = "$NAMESPACE:order"
            const val QUEUE_KEY = "$ORDER_PREFIX:queue"
            private const val QUEUE_ITEM_EXPIRE_PREFIX = "$QUEUE_KEY:expire:"
            const val PROCESSING_RATE_KEY = "$QUEUE_KEY:processing_rate"
            const val PROCESSING_COUNT_KEY = "$QUEUE_KEY:processing_count"
            private const val LOCK_PREFIX = "lock:"
            fun queueLockKey(userId: String): String = "$LOCK_PREFIX$QUEUE_KEY:$userId"
            fun queueItemExpireKey(userId: String): String = "$QUEUE_ITEM_EXPIRE_PREFIX$userId"
        }
    }
}
