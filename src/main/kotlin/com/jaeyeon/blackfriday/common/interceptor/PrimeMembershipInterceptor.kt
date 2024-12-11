package com.jaeyeon.blackfriday.common.interceptor

import com.jaeyeon.blackfriday.common.global.MemberException
import com.jaeyeon.blackfriday.common.security.annotation.PrimeOnly
import com.jaeyeon.blackfriday.common.security.session.SessionConstants.USER_KEY
import com.jaeyeon.blackfriday.common.security.session.SessionUser
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class PrimeMembershipInterceptor : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        if (handler !is HandlerMethod) return true

        if (!handler.hasMethodAnnotation(PrimeOnly::class.java)) return true

        val session = request.getSession(false)
            ?: throw MemberException.unauthorized()

        val sessionUser = session.getAttribute(USER_KEY) as? SessionUser
            ?: throw MemberException.unauthorized()

        if (!sessionUser.roles.contains("ROLE_PRIME")) {
            throw MemberException.unauthorized()
        }

        return true
    }
}
