package com.jaeyeon.blackfriday.domain.common.redis

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.testcontainers.containers.GenericContainer

class RedisTest : BehaviorSpec({

    val redisContainer = GenericContainer<Nothing>("redis:6.2.6-alpine").apply {
        withExposedPorts(6379)
        start()
    }

    val lettuceConnectionFactory = LettuceConnectionFactory(
        redisContainer.host,
        redisContainer.firstMappedPort,
    ).apply {
        afterPropertiesSet()
    }

    val redisTemplate = StringRedisTemplate(lettuceConnectionFactory)

    beforeSpec {
        redisTemplate.afterPropertiesSet()
    }

    afterSpec {
        lettuceConnectionFactory.destroy()
        redisContainer.stop()
    }

    beforeTest {
        redisTemplate.execute { connection ->
            connection.serverCommands().flushDb()
            true
        }
    }

    given("Redis 작업 검증") {
        `when`("Redis에 직접 추가할 때") {
            redisTemplate.execute { connection -> connection.serverCommands().flushDb(); true }

            val keyName = "test:key"
            redisTemplate.opsForValue().set(keyName, "test-value")
            val value = redisTemplate.opsForValue().get(keyName)

            then("Redis 작업이 정상 작동해야 함") {
                value shouldBe "test-value"
            }
        }
    }
})
