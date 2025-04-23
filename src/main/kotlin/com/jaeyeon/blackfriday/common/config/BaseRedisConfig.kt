package com.jaeyeon.blackfriday.common.config

import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession

@EnableRedisHttpSession(
    maxInactiveIntervalInSeconds = 3600,
    redisNamespace = "blackfriday:session",
)
abstract class BaseRedisConfig {

    protected val logger = KotlinLogging.logger {}

    abstract fun redisConnectionFactory(): RedisConnectionFactory

    @Bean
    @Primary
    open fun rateLimitRedisTemplate(): RedisTemplate<String, String> {
        logger.info { "Creating rateLimitRedisTemplate" }

        return RedisTemplate<String, String>().apply {
            connectionFactory = redisConnectionFactory()

            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()

            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()

            afterPropertiesSet()

            logger.info { "rateLimitRedisTemplate configured successfully" }
        }
    }

    @Bean
    open fun sessionRedisTemplate(
        springSessionDefaultRedisSerializer: RedisSerializer<Any>,
    ): RedisTemplate<String, Any> {
        val serializerClassName = springSessionDefaultRedisSerializer.javaClass.name
        logger.info {
            "Creating sessionRedisTemplate with serializer: $serializerClassName"
        }

        return RedisTemplate<String, Any>().apply {
            connectionFactory = redisConnectionFactory()

            keySerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()

            valueSerializer = springSessionDefaultRedisSerializer
            hashValueSerializer = springSessionDefaultRedisSerializer

            afterPropertiesSet()

            logger.info { "sessionRedisTemplate configured successfully" }
        }
    }
}
