package com.jaeyeon.blackfriday.common.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession

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
        val config = RedisStandaloneConfiguration(host, port).apply {
            setPassword(password)
        }
        return LettuceConnectionFactory(config)
    }

    @Bean
    fun springSessionDefaultRedisSerializer(): RedisSerializer<Any> {
        return GenericJackson2JsonRedisSerializer(createObjectMapper())
    }

    private fun createObjectMapper(): ObjectMapper {
        val typeValidator = BasicPolymorphicTypeValidator
            .builder()
            .allowIfBaseType(Any::class.java)
            .allowIfSubType(Any::class.java)
            .build()

        return ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL_AND_ENUMS,
                JsonTypeInfo.As.PROPERTY,
            )
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
}
