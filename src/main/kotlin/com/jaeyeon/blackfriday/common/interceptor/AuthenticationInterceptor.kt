package com.jaeyeon.blackfriday.common.interceptor

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
class AuthenticationInterceptor : HandlerInterceptor {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) return true

        val sessionId = request.getHeader(AUTH_HEADER)
            ?: throw MemberException.unauthorized()

        val session = request.getSession(false)
            ?: throw MemberException.unauthorized()

        if (session.id != sessionId) {
            throw MemberException.unauthorized()
        }

        val userAttribute = session.getAttribute(USER_KEY)
            ?: throw MemberException.unauthorized()

        log.info("[AuthInterceptor] USER attribute type: {}", userAttribute.javaClass.name)

        if (userAttribute !is SessionUser) {
            log.error(
                "[AuthInterceptor] Unexpected session attribute type: {}. Expected SessionUser",
                userAttribute.javaClass.name,
            )
            throw MemberException.unauthorized()
        }

        request.setAttribute(SESSION_USER_ATTRIBUTE, userAttribute)

        return true
    }
}
