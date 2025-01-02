package com.jaeyeon.blackfriday.domain.common

import com.jaeyeon.blackfriday.common.config.OrderNumberGenerator
import org.springframework.boot.test.context.TestComponent
import java.util.concurrent.atomic.AtomicLong

@TestComponent
class TestOrderNumberGenerator : OrderNumberGenerator() {
    private var sequence = AtomicLong(1L)

    override fun generate(): String {
        return "ORD${sequence.getAndIncrement()}"
    }
}
