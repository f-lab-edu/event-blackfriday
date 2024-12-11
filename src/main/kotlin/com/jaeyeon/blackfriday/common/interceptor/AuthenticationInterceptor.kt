package com.jaeyeon.blackfriday.common.interceptor

import com.jaeyeon.blackfriday.common.global.MemberException
import com.jaeyeon.blackfriday.common.security.session.SessionConstants.USER_KEY
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthenticationInterceptor : HandlerInterceptor {
    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) return true

        val sessionId = request.getHeader("X-Auth-Token")
            ?: throw MemberException.unauthorized()

        val session = request.getSession(false)
            ?: throw MemberException.unauthorized()

        if (session.id != sessionId) {
            throw MemberException.unauthorized()
        }

        if (session.getAttribute(USER_KEY) == null) {
            throw MemberException.unauthorized()
        }

        return true
    }
}
