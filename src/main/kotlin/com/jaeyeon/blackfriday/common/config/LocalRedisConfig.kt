package com.jaeyeon.blackfriday.common.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@Configuration
@EnableRedisHttpSession(
    maxInactiveIntervalInSeconds = 3600,
    redisNamespace = "blackfriday:session",
)
@Profile("local")
class LocalRedisConfig {
    companion object {
        val redisContainer = GenericContainer(DockerImageName.parse("redis:7.4.1-alpine")).apply {
            withExposedPorts(6379)
            start()
        }
    }

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration().apply {
            hostName = redisContainer.host
            port = redisContainer.firstMappedPort
        }
        return LettuceConnectionFactory(config)
    }

    @Bean
    @Primary
    fun rateLimitRedisTemplate(): RedisTemplate<String, String> {
        return RedisTemplate<String, String>().apply {
            connectionFactory = redisConnectionFactory()
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()
            hashValueSerializer = StringRedisSerializer()
            afterPropertiesSet()
        }
    }

    @Bean
    fun sessionRedisTemplate(springSessionDefaultRedisSerializer: RedisSerializer<Any>): RedisTemplate<String, Any> {
        log.info(
            "[LocalRedisConfig] Creating sessionRedisTemplate with serializer: {}",
            springSessionDefaultRedisSerializer.javaClass.name,
        )

        return RedisTemplate<String, Any>().apply {
            connectionFactory = redisConnectionFactory()

            keySerializer = StringRedisSerializer()
            hashKeySerializer = StringRedisSerializer()

            valueSerializer = springSessionDefaultRedisSerializer
            hashValueSerializer = springSessionDefaultRedisSerializer

            afterPropertiesSet()

            log.info("[LocalRedisConfig] sessionRedisTemplate configured successfully")
        }
    }
}
