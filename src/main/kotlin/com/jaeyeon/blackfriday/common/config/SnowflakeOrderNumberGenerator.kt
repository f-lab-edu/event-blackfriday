package com.jaeyeon.blackfriday.common.config

import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

interface NumberGenerator {
    fun generate(): String
}

abstract class SnowflakeGenerator {
    companion object {
        const val SEQUENCE_BITS = 12L
        const val EPOCH_TIMESTAMP = 1704034800000L

        const val INITIAL_SEQUENCE = 0
        const val INITIAL_TIMESTAMP = -1L
        const val MINUS_ONE = -1L
        const val ZERO = 0
    }

    private val sequenceMask = MINUS_ONE xor (MINUS_ONE shl SEQUENCE_BITS.toInt())
    private val sequence = AtomicInteger(INITIAL_SEQUENCE)
    private var lastTimestamp = AtomicLong(INITIAL_TIMESTAMP)

    @Synchronized
    protected fun nextId(): Long {
        var timestamp = System.currentTimeMillis()
        val currentLastTimestamp = lastTimestamp.get()

        check(timestamp >= currentLastTimestamp) { "Clock moved backwards. Refusing to generate id" }

        if (timestamp == currentLastTimestamp) {
            val currentSequence = sequence.getAndIncrement() and sequenceMask.toInt()
            if (currentSequence == ZERO) {
                timestamp = waitNextMillis(currentLastTimestamp)
            }
        } else {
            sequence.set(ZERO)
        }

        lastTimestamp.set(timestamp)

        return ((timestamp - EPOCH_TIMESTAMP) shl SEQUENCE_BITS.toInt()) or sequence.get().toLong()
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
    companion object {
        const val PREFIX = "ORD"
    }

    override fun generate(): String = "$PREFIX${nextId()}"
}

@Component
class PaymentNumberGenerator : SnowflakeGenerator(), NumberGenerator {
    companion object {
        const val PREFIX = "PAY"
    }

    override fun generate(): String = "$PREFIX${nextId()}"
}
