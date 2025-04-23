package com.jaeyeon.blackfriday.common.config

import io.lettuce.core.ClientOptions
import io.lettuce.core.protocol.ProtocolVersion
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import java.time.Duration.ZERO
import java.time.Duration.ofSeconds

@Configuration
@EnableRedisHttpSession(
    maxInactiveIntervalInSeconds = 3600,
    redisNamespace = "blackfriday:session",
)
@Profile("prod")
class RedisConfig(
    @Value("\${spring.data.redis.host}") private val configRedisHost: String,
    @Value("\${spring.data.redis.port}") private val configRedisPort: Int,
    @Value("\${spring.data.redis.password}") private val configRedisPassword: String,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        LettuceClientConfiguration.builder()
            .clientName("blackfriday-session")
            .commandTimeout(ofSeconds(2))
            .shutdownTimeout(ZERO)
            .clientOptions(
                ClientOptions.builder()
                    .protocolVersion(ProtocolVersion.RESP2)
                    .build(),
            )
            .build()

        log.info("Redis 연결 설정 시작 - host: {}, port: {}", configRedisHost, configRedisPort)

        val config = RedisStandaloneConfiguration().apply {
            hostName = configRedisHost
            this.port = configRedisPort
            setPassword(configRedisPassword)
        }

        log.info("Redis 연결 팩토리 생성 완료")
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
