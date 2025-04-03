package com.jaeyeon.blackfriday.common.resolver

import com.jaeyeon.blackfriday.common.global.MemberException
import com.jaeyeon.blackfriday.common.security.annotation.CurrentUser
import com.jaeyeon.blackfriday.common.security.session.SessionConstants.SESSION_USER_ATTRIBUTE
import com.jaeyeon.blackfriday.common.security.session.SessionUser
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class CurrentUserArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(CurrentUser::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any? {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw MemberException.unauthorized()

        val sessionUser = request.getAttribute(SESSION_USER_ATTRIBUTE) as? SessionUser
            ?: throw MemberException.unauthorized()

        return sessionUser.id
    }
}
