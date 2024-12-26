package com.jaeyeon.blackfriday.common.config

import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

interface NumberGenerator {
    fun generate(): String
}

abstract class SnowflakeGenerator {
    protected val epochTimestamp = 1704034800000L
    protected val sequenceBits = 12L
    protected val sequenceMask = -1L xor (-1L shl sequenceBits.toInt())

    protected val sequence = AtomicInteger(0)
    protected var lastTimestamp = AtomicLong(-1L)

    @Synchronized
    protected fun nextId(): Long {
        var timestamp = System.currentTimeMillis()
        val currentLastTimestamp = lastTimestamp.get()

        check(timestamp >= currentLastTimestamp) { "Clock moved backwards. Refusing to generate id" }

        if (timestamp == currentLastTimestamp) {
            val currentSequence = sequence.getAndIncrement() and sequenceMask.toInt()
            if (currentSequence == 0) {
                timestamp = waitNextMillis(currentLastTimestamp)
            }
        } else {
            sequence.set(0)
        }

        lastTimestamp.set(timestamp)

        return ((timestamp - epochTimestamp) shl sequenceBits.toInt()) or sequence.get().toLong()
    }

    private fun waitNextMillis(lastTimestamp: Long): Long {
        var timestamp = System.currentTimeMillis()
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis()
        }
        return timestamp
    }
}

@Component
class OrderNumberGenerator : SnowflakeGenerator(), NumberGenerator {
    override fun generate(): String = "ORD${nextId()}"
}

@Component
class PaymentNumberGenerator : SnowflakeGenerator(), NumberGenerator {
    override fun generate(): String = "PAY${nextId()}"
}
