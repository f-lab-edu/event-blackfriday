package com.jaeyeon.blackfriday.common.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import com.jaeyeon.blackfriday.common.const.HttpConstants.RateLimit
import com.jaeyeon.blackfriday.common.exception.ErrorCode
import com.jaeyeon.blackfriday.common.exception.ErrorResponse
import com.jaeyeon.blackfriday.common.global.MemberException
import com.jaeyeon.blackfriday.common.ratelimit.OrderRateLimiter
import com.jaeyeon.blackfriday.common.security.session.SessionConstants.USER_KEY
import com.jaeyeon.blackfriday.common.security.session.SessionUser
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.ZoneOffset

@Component
class OrderRateLimitInterceptor(
    private val rateLimiter: OrderRateLimiter,
    private val objectMapper: ObjectMapper,
) : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val sessionUser = request.session.getAttribute(USER_KEY) as? SessionUser
            ?: throw MemberException.unauthorized()

        val userId = sessionUser.id.toString()

        if (!rateLimiter.tryConsume(userId)) {
            log.warn("Rate limit exceeded for user: $userId (email: ${sessionUser.email})")

            val rateLimitInfo = rateLimiter.getRateLimitInfo(userId)

            response.apply {
                status = HttpStatus.TOO_MANY_REQUESTS.value()
                setHeader(RateLimit.HEADER_LIMIT, rateLimitInfo.limit.toString())
                setHeader(RateLimit.HEADER_REMAINING, rateLimitInfo.remaining.toString())
                setHeader(
                    RateLimit.HEADER_RESET,
                    rateLimitInfo.resetTime.toEpochSecond(ZoneOffset.UTC).toString(),
                )
                contentType = MediaType.APPLICATION_JSON_VALUE
            }

            val errorResponse = ErrorResponse.of(ErrorCode.TOO_MANY_REQUESTS)
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
            return false
        }

        return true
    }
}
