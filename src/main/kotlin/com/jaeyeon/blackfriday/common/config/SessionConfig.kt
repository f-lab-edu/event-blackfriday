package com.jaeyeon.blackfriday.common.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.session.web.http.HeaderHttpSessionIdResolver

@Configuration
class SessionConfig {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun springSessionRepositoryRedisSerializer(springSessionDefaultRedisSerializer: RedisSerializer<Any>):
        RedisSerializer<Any> {
        log.info("SessionConfig initialized with custom Redis serializer")
        return springSessionDefaultRedisSerializer
    }

    @Bean
    fun httpSessionIdResolver(): HeaderHttpSessionIdResolver {
        log.info("Creating HeaderHttpSessionIdResolver.xAuthToken()")
        return HeaderHttpSessionIdResolver("X-Auth-Token")
    }
}
