package com.jaeyeon.blackfriday.common.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jaeyeon.blackfriday.common.security.session.SessionUser
import io.lettuce.core.ClientOptions
import io.lettuce.core.protocol.ProtocolVersion
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession
import java.time.Duration.ZERO
import java.time.Duration.ofSeconds

@Configuration
@EnableRedisHttpSession(
    maxInactiveIntervalInSeconds = 3600,
    redisNamespace = "blackfriday:session",
)
class RedisConfig(
    @Value("\${spring.data.redis.host}") private val host: String,
    @Value("\${spring.data.redis.port}") private val port: Int,
    @Value("\${spring.data.redis.password}") private val password: String,
) {
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val clientConfig = LettuceClientConfiguration.builder()
            .clientName("blackfriday-session")
            .commandTimeout(ofSeconds(2))
            .shutdownTimeout(ZERO)
            .clientOptions(
                ClientOptions.builder()
                    .protocolVersion(ProtocolVersion.RESP2)
                    .build(),
            )
            .build()

        val config = RedisStandaloneConfiguration(host, port).apply {
            setPassword(password)
        }

        return LettuceConnectionFactory(config, clientConfig)
    }

    @Bean
    fun springSessionDefaultRedisSerializer(): RedisSerializer<Any> {
        val objectMapper = ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            val typeValidator = BasicPolymorphicTypeValidator
                .builder()
                .allowIfBaseType(SessionUser::class.java)
                .allowIfSubType(Any::class.java)
                .build()

            activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL_AND_ENUMS,
                JsonTypeInfo.As.PROPERTY,
            )
        }
        return GenericJackson2JsonRedisSerializer(objectMapper)
    }
}
