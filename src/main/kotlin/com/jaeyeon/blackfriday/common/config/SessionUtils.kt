package com.jaeyeon.blackfriday.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.jaeyeon.blackfriday.common.global.MemberException
import com.jaeyeon.blackfriday.common.security.session.SessionUser
import mu.KotlinLogging

object SessionUtils {
    private val logger = KotlinLogging.logger {}
    private val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        registerModule(JavaTimeModule())
    }

    fun convertToSessionUser(userAttribute: Any?, source: String): SessionUser {
        return when (userAttribute) {
            is SessionUser -> userAttribute
            is Map<*, *> -> convertMapToSessionUser(userAttribute)
            else -> {
                logger.error {
                    "[$source] Unexpected session attribute type: ${userAttribute?.javaClass?.name}. " +
                        "Expected SessionUser or Map"
                }
                throw MemberException.unauthorized()
            }
        }
    }

    private fun convertMapToSessionUser(map: Map<*, *>): SessionUser {
        return try {
            objectMapper.convertValue(map, SessionUser::class.java)
        } catch (e: Exception) {
            logger.error { "Failed to convert Map to SessionUser: ${e.message}" }
            throw MemberException.unauthorized()
        }
    }
}
