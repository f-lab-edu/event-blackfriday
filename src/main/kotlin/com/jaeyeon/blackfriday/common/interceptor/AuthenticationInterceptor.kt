package com.jaeyeon.blackfriday.common.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import com.jaeyeon.blackfriday.common.global.MemberException
import com.jaeyeon.blackfriday.common.security.session.SecurityConstants.AUTH_HEADER
import com.jaeyeon.blackfriday.common.security.session.SessionConstants.SESSION_USER_ATTRIBUTE
import com.jaeyeon.blackfriday.common.security.session.SessionConstants.USER_KEY
import com.jaeyeon.blackfriday.common.security.session.SessionUser
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthenticationInterceptor(
    private val objectMapper: ObjectMapper,
) : HandlerInterceptor {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) return true

        val sessionId = request.getHeader(AUTH_HEADER)
        log.info("[AuthInterceptor] Incoming token: {}", sessionId)

        if (sessionId == null) {
            log.warn("[AuthInterceptor] Token is null!")
            throw MemberException.unauthorized()
        }

        val session = request.getSession(false)
        if (session == null) {
            log.warn("[AuthInterceptor] Session not found for token: {}", sessionId)
            throw MemberException.unauthorized()
        }
        log.info("[AuthInterceptor] Session found: ID={}, isNew={}", session.id, session.isNew)

        val userAttribute = session.getAttribute(USER_KEY)
        if (userAttribute == null) {
            log.warn("[AuthInterceptor] USER attribute is null in session: {}", session.id)
            throw MemberException.unauthorized()
        }
        log.info("[AuthInterceptor] USER attribute found: {}", userAttribute::class.java.simpleName)

        val sessionUser = when (userAttribute) {
            is SessionUser -> userAttribute
            is Map<*, *> -> {
                try {
                    log.info("[AuthInterceptor] Converting Map to SessionUser")
                    objectMapper.convertValue(userAttribute, SessionUser::class.java)
                } catch (e: Exception) {
                    log.error("[AuthInterceptor] Failed to convert Map to SessionUser", e)
                    throw MemberException.unauthorized()
                }
            }
            else -> {
                log.error(
                    "[AuthInterceptor] Unexpected session attribute type: {}",
                    userAttribute::class.java.name,
                )
                throw MemberException.unauthorized()
            }
        }

        request.setAttribute(SESSION_USER_ATTRIBUTE, sessionUser)

        return true
    }
}
