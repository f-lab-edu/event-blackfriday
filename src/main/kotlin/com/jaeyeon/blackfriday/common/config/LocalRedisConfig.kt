package com.jaeyeon.blackfriday.common.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jaeyeon.blackfriday.common.security.session.SessionUser
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
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
