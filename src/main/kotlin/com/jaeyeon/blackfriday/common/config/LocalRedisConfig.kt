package com.jaeyeon.blackfriday.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@Configuration
@EnableRedisHttpSession(
    maxInactiveIntervalInSeconds = 3600,
    redisNamespace = "blackfriday:session",
)
@Profile("local")
class LocalRedisConfig : BaseRedisConfig() {
    companion object {
        val redisContainer = GenericContainer(DockerImageName.parse("redis:7.4.1-alpine")).apply {
            withExposedPorts(6379)
            start()
        }
    }

    @Bean
    override fun redisConnectionFactory(): RedisConnectionFactory {
        logger.info { "Local Redis 연결 설정 - host: ${redisContainer.host}, port: ${redisContainer.firstMappedPort}" }

        val config = RedisStandaloneConfiguration().apply {
            hostName = redisContainer.host
            port = redisContainer.firstMappedPort
        }

        return LettuceConnectionFactory(config).apply {
            afterPropertiesSet()
        }
    }
}
