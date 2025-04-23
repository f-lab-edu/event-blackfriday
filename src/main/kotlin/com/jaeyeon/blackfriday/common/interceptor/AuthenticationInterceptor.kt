package com.jaeyeon.blackfriday.common.interceptor

import com.jaeyeon.blackfriday.common.config.SessionUtils
import com.jaeyeon.blackfriday.common.global.MemberException
import com.jaeyeon.blackfriday.common.security.session.SecurityConstants.AUTH_HEADER
import com.jaeyeon.blackfriday.common.security.session.SessionConstants.SESSION_USER_ATTRIBUTE
import com.jaeyeon.blackfriday.common.security.session.SessionConstants.USER_KEY
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthenticationInterceptor : HandlerInterceptor {
    private val logger = KotlinLogging.logger {}

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) return true

        val sessionId = request.getHeader(AUTH_HEADER)
        logger.info { "[AuthInterceptor] Incoming token: $sessionId" }

        if (sessionId == null) {
            logger.warn { "[AuthInterceptor] Token is null!" }
            throw MemberException.unauthorized()
        }

        val session = request.getSession(false)
        if (session == null) {
            logger.warn { "[AuthInterceptor] Session not found for token: $sessionId" }
            throw MemberException.unauthorized()
        }
        logger.info { "[AuthInterceptor] Session found: ID=${session.id}, isNew=${session.isNew}" }

        val userAttribute = session.getAttribute(USER_KEY)
        if (userAttribute == null) {
            logger.warn { "[AuthInterceptor] USER attribute is null in session: ${session.id}" }
            throw MemberException.unauthorized()
        }
        logger.info { "[AuthInterceptor] USER attribute type: ${userAttribute.javaClass.name}" }

        val sessionUser = SessionUtils.convertToSessionUser(userAttribute, "AuthInterceptor")

        request.setAttribute(SESSION_USER_ATTRIBUTE, sessionUser)

        return true
    }
}
