package com.jaeyeon.blackfriday.common.config

import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.web.http.HeaderHttpSessionIdResolver
import org.springframework.session.web.http.HttpSessionIdResolver

@Configuration
class SessionConfig {
    private val log = LoggerFactory.getLogger(SessionConfig::class.java)

    @PostConstruct
    fun init() {
        log.info("SessionConfig initialized with X-Auth-Token resolver")
    }

    @Bean
    fun httpSessionIdResolver(): HttpSessionIdResolver {
        log.info("Creating HeaderHttpSessionIdResolver.xAuthToken()")
        return HeaderHttpSessionIdResolver.xAuthToken()
    }
}
