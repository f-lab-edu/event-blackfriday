package com.jaeyeon.blackfriday.common.global

import com.jaeyeon.blackfriday.common.exception.ErrorCode
import com.jaeyeon.blackfriday.common.exception.ErrorResponse
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BlackFridayException::class)
    fun handleBlackFridayException(ex: BlackFridayException): ResponseEntity<ErrorResponse> {
        log.warn("Business exception occurred: ${ex.errorCode}", ex)
        val response = ErrorResponse.of(ex.errorCode)
        return ResponseEntity.status(ex.errorCode.status).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        log.debug("Validation failed: {}", ex.bindingResult)
        val response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, ex.bindingResult)
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.status).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected error occurred", ex)
        val response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR)
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.status).body(response)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException): ResponseEntity<ErrorResponse> {
        log.debug("No handler found for {}", ex.requestURL)
        val response = ErrorResponse.of(ErrorCode.NOT_FOUND)
        return ResponseEntity.status(ErrorCode.NOT_FOUND.status).body(response)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        log.debug("Constraint violation: {}", ex.constraintViolations)
        val response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE)
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.status).body(response)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(
        ex: HttpRequestMethodNotSupportedException,
    ): ResponseEntity<ErrorResponse> {
        log.debug("Method not allowed: {}", ex.message)
        val response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED)
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.status).body(response)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        log.debug("Message not readable: {}", ex.message)
        val response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE)
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.status).body(response)
    }

    @ExceptionHandler(RedisConnectionFailureException::class)
    fun handleRedisConnectionFailureException(ex: RedisConnectionFailureException): ResponseEntity<ErrorResponse> {
        log.error("Redis connection failed", ex)
        val response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR)
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.status).body(response)
    }
}
