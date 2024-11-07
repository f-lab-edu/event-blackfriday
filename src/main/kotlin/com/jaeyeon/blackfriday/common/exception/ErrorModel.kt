package com.jaeyeon.blackfriday.common.exception

import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C-001", "유효하지 않은 입력입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C-002", "서버 오류가 발생했습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C-003", "페이지를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C-004", "지원하지 않는 HTTP 메소드입니다."),

    // Product
    INVALID_PRODUCT_NAME(HttpStatus.BAD_REQUEST, "P-001", "유효하지 않는 상품명입니다."),
    INVALID_PRODUCT_DESCRIPTION(HttpStatus.BAD_REQUEST, "P-002", "유효하지 않는 상품 설명입니다."),
    INVALID_PRODUCT_PRICE(HttpStatus.BAD_REQUEST, "P-003", "유효하지 않는 가격입니다."),
    INVALID_PRODUCT_STOCK_QUANTITY(HttpStatus.BAD_REQUEST, "P-004", "유효하지 않는 재고 수량입니다."),
    NOT_ENOUGH_STOCK(HttpStatus.BAD_REQUEST, "P-005", "재고가 부족합니다."),
}

data class ErrorResponse(
    val status: Int,
    val code: String,
    val message: String,
    val errors: List<FieldError>? = null,
) {
    companion object {
        fun of(errorCode: ErrorCode, errors: List<FieldError>? = null): ErrorResponse {
            return ErrorResponse(
                status = errorCode.status.value(),
                code = errorCode.code,
                message = errorCode.message,
                errors = errors,
            )
        }

        fun of(errorCode: ErrorCode, bindingResult: BindingResult): ErrorResponse {
            return ErrorResponse(
                status = errorCode.status.value(),
                code = errorCode.code,
                message = errorCode.message,
                errors = FieldError.of(bindingResult),
            )
        }
    }

    data class FieldError(
        val field: String,
        val rejectedValue: Any?,
        val reason: String,
    ) {
        companion object {
            fun of(bindingResult: BindingResult): List<FieldError> {
                return bindingResult.fieldErrors.map { error ->
                    FieldError(
                        field = error.field,
                        rejectedValue = error.rejectedValue,
                        reason = error.defaultMessage ?: "",
                    )
                }
            }
        }
    }
}
