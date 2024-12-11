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
    INVALID_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT-006", "상품을 찾을 수 없습니다."),

    INVALID_CATEGORY_NAME(HttpStatus.BAD_REQUEST, "CATEGORY-001", "카테고리 이름은 2-50자 사이여야 합니다."),
    INVALID_CATEGORY_DEPTH(HttpStatus.BAD_REQUEST, "CATEGORY-002", "카테고리는 최대 4단계까지만 허용됩니다."),
    INVALID_CATEGORY_DISPLAY_ORDER(HttpStatus.BAD_REQUEST, "CATEGORY-003", "노출 순서는 0 이상이어야 합니다"),
    INVALID_CATEGORY_CLOSURE_DEPTH(HttpStatus.BAD_REQUEST, "CATEGORY-004", "Closure depth는 0 이상이어야 합니다"),
    INVALID_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY-005", "카테고리를 찾을 수 없습니다."),
    INVALID_CATEGORY_DUPLICATE_NAME(HttpStatus.BAD_REQUEST, "CATEGORY-006", "중복된 카테고리 이름입니다."),

    INVALID_MEMBER_EMAIL(HttpStatus.BAD_REQUEST, "MEMBER-001", "유효하지 않은 이메일입니다."),
    INVALID_MEMBER_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER-002", "유효하지 않은 비밀번호입니다."),
    INVALID_MEMBER_NAME(HttpStatus.BAD_REQUEST, "MEMBER-003", "유효하지 않은 이름입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-004", "회원을 찾을 수 없습니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER-005", "이미 존재하는 회원입니다."),
    ALREADY_SUBSCRIBED(HttpStatus.BAD_REQUEST, "MEMBER-006", "이미 구독 중인 회원입니다."),
    ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "MEMBER-007", "이미 탈퇴한 회원입니다."),
    NOT_SUBSCRIBED(HttpStatus.BAD_REQUEST, "MEMBER-008", "구독 중이 아닌 회원입니다."),
    EXPIRED_PRIME_MEMBERSHIP(HttpStatus.BAD_REQUEST, "MEMBER-009", "만료된 프라임 멤버십입니다."),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-001", "인증이 필요합니다."),
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
