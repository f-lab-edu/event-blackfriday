package com.jaeyeon.blackfriday.common.global

import com.jaeyeon.blackfriday.common.exception.ErrorCode
import com.jaeyeon.blackfriday.common.exception.ErrorResponse
import jakarta.validation.ConstraintViolationException
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BlackFridayException::class)
    fun handleBlackFridayException(ex: BlackFridayException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse.of(ex.errorCode)
        return ResponseEntity.status(ex.errorCode.status).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, ex.bindingResult)
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.status).body(response)
    }

    @ExceptionHandler(Exception::class)
    fun handleException(ex: Exception): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR)
        return ResponseEntity.status(ErrorCode.INTERNAL_SERVER_ERROR.status).body(response)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse.of(ErrorCode.NOT_FOUND)
        return ResponseEntity.status(ErrorCode.NOT_FOUND.status).body(response)
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(ex: ConstraintViolationException): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE)
        return ResponseEntity.status(ErrorCode.INVALID_INPUT_VALUE.status).body(response)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(
        ex: HttpRequestMethodNotSupportedException,
    ): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED)
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.status).body(response)
    }
}
