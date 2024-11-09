package com.jaeyeon.blackfriday.common.exception

import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-001", "유효하지 않은 입력입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-002", "서버 오류가 발생했습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-003", "페이지를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-004", "지원하지 않는 HTTP 메소드입니다."),

    INVALID_PRODUCT_NAME(HttpStatus.BAD_REQUEST, "PRODUCT-001", "유효하지 않는 상품명입니다."),
    INVALID_PRODUCT_DESCRIPTION(HttpStatus.BAD_REQUEST, "PRODUCT-002", "유효하지 않는 상품 설명입니다."),
    INVALID_PRODUCT_PRICE(HttpStatus.BAD_REQUEST, "PRODUCT-003", "유효하지 않는 가격입니다."),
    INVALID_PRODUCT_STOCK_QUANTITY(HttpStatus.BAD_REQUEST, "PRODUCT-004", "유효하지 않는 재고 수량입니다."),
    NOT_ENOUGH_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT-005", "재고가 부족합니다."),

    INVALID_CATEGORY_NAME(HttpStatus.BAD_REQUEST, "CATEGORY-001", "카테고리 이름은 2-50자 사이여야 합니다."),
    INVALID_CATEGORY_DEPTH(HttpStatus.BAD_REQUEST, "CATEGORY-002", "카테고리는 최대 4단계까지만 허용됩니다."),
    INVALID_CATEGORY_DISCOUNT_RATE(HttpStatus.BAD_REQUEST, "CATEGORY-003", "할인율은 0-90% 범위여야 합니다."),
}

data class ErrorResponse(
    val code: String,
    val message: String,
    val errors: List<FieldError>? = null,
) {
    companion object {
        fun of(errorCode: ErrorCode, errors: List<FieldError>? = null): ErrorResponse {
            return ErrorResponse(
                code = errorCode.code,
                message = errorCode.message,
                errors = errors,
            )
        }

        fun of(errorCode: ErrorCode, bindingResult: BindingResult): ErrorResponse {
            return ErrorResponse(
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
