package com.jaeyeon.blackfriday.common.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
class RedissonConfig {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    @Profile("prod")
    fun prodRedissonClient(
        @Value("\${spring.data.redis.host}") host: String,
        @Value("\${spring.data.redis.port}") port: Int,
        @Value("\${spring.data.redis.password}") password: String,
        @Value("\${spring.data.redis.timeout}") timeout: Int,
        @Value("\${spring.data.redis.lettuce.pool.max-active:8}") maxActive: Int,
        @Value("\${spring.data.redis.lettuce.pool.max-idle:4}") maxIdle: Int,
        @Value("\${spring.data.redis.lettuce.pool.min-idle:2}") minIdle: Int,
    ): RedissonClient {
        log.info("프로덕션 환경 Redisson 클라이언트 설정 - host: {}, port: {}", host, port)

        val config = Config()
        config.useSingleServer()
            .setAddress("redis://$host:$port")
            .setPassword(password)
            .setConnectionMinimumIdleSize(minIdle)
            .setConnectionPoolSize(maxActive)
            .setIdleConnectionTimeout(timeout)
            .setConnectTimeout(timeout)
            .setDatabase(0)
            .setClientName("blackfriday-redisson")

        log.info("프로덕션 Redisson 클라이언트 생성 완료")
        return Redisson.create(config)
    }

    @Bean
    @Profile("local")
    fun localRedissonClient(
        @Value("\${spring.data.redis.host:localhost}") host: String,
        @Value("\${spring.data.redis.port:6379}") port: Int,
    ): RedissonClient {
        val containerHost = LocalRedisConfig.redisContainer.host
        val containerPort = LocalRedisConfig.redisContainer.firstMappedPort

        log.info("로컬 환경 Redisson 클라이언트 설정 - host: {}, port: {}", host, port)

        val config = Config()
        config.useSingleServer()
            .setAddress("redis://$containerHost:$containerPort")
            .setDatabase(0)
            .setConnectionMinimumIdleSize(1)
            .setConnectionPoolSize(2)
            .setConnectTimeout(2000)
            .setClientName("blackfriday-redisson-local")

        log.info("로컬 Redisson 클라이언트 생성 완료")
        return Redisson.create(config)
    }
}
