package com.jaeyeon.blackfriday.common.config

import io.lettuce.core.ClientOptions
import io.lettuce.core.protocol.ProtocolVersion
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import java.time.Duration

@Configuration
@Profile("prod")
class ProdRedisConfig(
    @Value("\${spring.data.redis.host}") private val redisHost: String,
    @Value("\${spring.data.redis.port}") private val redisPort: Int,
    @Value("\${spring.data.redis.password}") private val redisPassword: String,
) : BaseRedisConfig() {

    @Bean
    override fun redisConnectionFactory(): RedisConnectionFactory {
        logger.info { "Production Redis 연결 설정 - host: $redisHost, port: $redisPort" }

        val clientConfig = LettuceClientConfiguration.builder()
            .clientName("blackfriday-session")
            .commandTimeout(Duration.ofSeconds(2))
            .shutdownTimeout(Duration.ZERO)
            .clientOptions(
                ClientOptions.builder()
                    .protocolVersion(ProtocolVersion.RESP2)
                    .build(),
            )
            .build()

        val config = RedisStandaloneConfiguration().apply {
            hostName = redisHost
            port = redisPort
            setPassword(redisPassword)
        }

        return LettuceConnectionFactory(config, clientConfig).apply {
            afterPropertiesSet()
        }
    }
}
