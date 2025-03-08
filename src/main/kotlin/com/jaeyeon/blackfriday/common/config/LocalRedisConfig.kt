package com.jaeyeon.blackfriday.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@Configuration
@Profile("local")
class LocalRedisConfig {
    private val redisContainer = GenericContainer(DockerImageName.parse("redis:7.4.1-alpine")).apply {
        withExposedPorts(6379)
        start()
    }

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
        }
    }
}
