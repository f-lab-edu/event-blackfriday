package com.jaeyeon.blackfriday.common.ratelimit

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Component
class RedisOrderRateLimiter(
    private val redisTemplate: RedisTemplate<String, String>,
    private val properties: RateLimitProperties,
) : OrderRateLimiter {

    // Reids Aotomic Increment를 이용한 Rate Limit 구현
    // counter기반
    // 토큰 버킷 방식과 비슷한 방식
    private val rateLimitScript = """
        local key = KEYS[1]
        local limit = tonumber(ARGV[1])
        local window = tonumber(ARGV[2])
        local current = redis.call('incr', key)
        
        if current == 1 then
            redis.call('expire', key, window)
        end
        
        if current > limit then
            return 0
        end
        
        return current
    """

    override fun tryConsume(userId: String): Boolean {
        val key = "rate:order:$userId"
        val result = redisTemplate.execute(
            DefaultRedisScript(rateLimitScript, Long::class.java),
            listOf(key),
            properties.requestsPerSecond.toString(),
            properties.windowSizeInSeconds.toString(),
        )
        return result <= properties.requestsPerSecond
    }

    override fun getRateLimitInfo(userId: String): RateLimitInfo {
        val key = "rate:order:$userId"
        val current = redisTemplate.opsForValue().get(key)?.toIntOrNull() ?: 0
        val ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS)

        return RateLimitInfo(
            limit = properties.requestsPerSecond,
            remaining = (properties.requestsPerSecond - current).coerceAtLeast(0),
            resetTime = LocalDateTime.now().plusSeconds(ttl.coerceAtLeast(0)),
            windowSizeInSeconds = properties.windowSizeInSeconds,
        )
    }
}

// 남은 시간은 일단 제거하고 -> 테스트코드 수정 -> 부하테스트(테스트) <- shell 스크립트로 작성, EX) K6
// 인스턴스 부하를 주는 테스트를 해봐야된다.
// Tomcat 내장 Thread 개수를 확인하고 느려지는지 봐야 한다.
// 우리 프로젝트가 바로 스케일링이 되는 구조인가? -> "TIMEOUT이 발생하는지 확인해야 한다." GCP 모니터링 툴
// NGINX 서버 분리필요.
// 인스턴스가 죽을 정도로만? 애플리케이션 서버 띄울때 JVM 적게 (HEAP), EX) 500MB, 250MB... ThreadPool 꽉참 -> 이후 요청 Queue에 쌓임. 한 개의 EC2에 두 개의 포트가 필요

// EC2 (NGINX, LB)... -> NGINX(docker) <--> EC2(docker) <--> MySQL(docker) <--> REDIS(docker)
