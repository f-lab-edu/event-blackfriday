package com.jaeyeon.blackfriday.common.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer

@Configuration
class ObjectMapperConfig {

    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }

    @Bean
    fun redisObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(KotlinModule.Builder().build())
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

            configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)

            val typeValidator = BasicPolymorphicTypeValidator
                .builder()
                .allowIfBaseType(Any::class.java)
                .build()

            activateDefaultTyping(
                typeValidator,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY,
            )
        }
    }

    @Bean
    fun springSessionDefaultRedisSerializer(redisObjectMapper: ObjectMapper): RedisSerializer<Any> {
        return GenericJackson2JsonRedisSerializer(redisObjectMapper)
    }
}
