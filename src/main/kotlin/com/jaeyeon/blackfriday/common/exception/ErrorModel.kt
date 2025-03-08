package com.jaeyeon.blackfriday.common.exception

import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String,
) {
    // Common Errors
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON-001", "유효하지 않은 입력입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-002", "서버 오류가 발생했습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON-003", "페이지를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON-004", "지원하지 않는 HTTP 메소드입니다."),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "COMMON-005", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요."),

    // Product Errors
    INVALID_PRODUCT_NAME(HttpStatus.BAD_REQUEST, "PRODUCT-001", "유효하지 않는 상품명입니다."),
    INVALID_PRODUCT_DESCRIPTION(HttpStatus.BAD_REQUEST, "PRODUCT-002", "유효하지 않는 상품 설명입니다."),
    INVALID_PRODUCT_PRICE(HttpStatus.BAD_REQUEST, "PRODUCT-003", "유효하지 않는 가격입니다."),
    INVALID_PRODUCT_STOCK_QUANTITY(HttpStatus.BAD_REQUEST, "PRODUCT-004", "유효하지 않는 재고 수량입니다."),
    NOT_ENOUGH_STOCK(HttpStatus.BAD_REQUEST, "PRODUCT-005", "재고가 부족합니다."),
    INVALID_PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT-006", "상품을 찾을 수 없습니다."),
    PRODUCT_NOT_OWNER(HttpStatus.BAD_REQUEST, "PRODUCT-007", "상품의 소유자가 아닙니다."),

    // Category Errors
    INVALID_CATEGORY_NAME(HttpStatus.BAD_REQUEST, "CATEGORY-001", "카테고리 이름은 2-50자 사이여야 합니다."),
    INVALID_CATEGORY_DEPTH(HttpStatus.BAD_REQUEST, "CATEGORY-002", "카테고리는 최대 4단계까지만 허용됩니다."),
    INVALID_CATEGORY_DISPLAY_ORDER(HttpStatus.BAD_REQUEST, "CATEGORY-003", "노출 순서는 0 이상이어야 합니다"),
    INVALID_CATEGORY_CLOSURE_DEPTH(HttpStatus.BAD_REQUEST, "CATEGORY-004", "Closure depth는 0 이상이어야 합니다"),
    INVALID_CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "CATEGORY-005", "카테고리를 찾을 수 없습니다."),
    INVALID_CATEGORY_DUPLICATE_NAME(HttpStatus.BAD_REQUEST, "CATEGORY-006", "중복된 카테고리 이름입니다."),
    CATEGORY_NOT_OWNER(HttpStatus.BAD_REQUEST, "CATEGORY-007", "카테고리의 소유자가 아닙니다."),

    // Member Errors
    INVALID_MEMBER_EMAIL(HttpStatus.BAD_REQUEST, "MEMBER-001", "유효하지 않은 이메일입니다."),
    INVALID_MEMBER_PASSWORD(HttpStatus.BAD_REQUEST, "MEMBER-002", "유효하지 않은 비밀번호입니다."),
    INVALID_MEMBER_NAME(HttpStatus.BAD_REQUEST, "MEMBER-003", "유효하지 않은 이름입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER-004", "회원을 찾을 수 없습니다."),
    MEMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MEMBER-005", "이미 존재하는 회원입니다."),
    ALREADY_SUBSCRIBED(HttpStatus.BAD_REQUEST, "MEMBER-006", "이미 구독 중인 회원입니다."),
    ALREADY_WITHDRAWN(HttpStatus.BAD_REQUEST, "MEMBER-007", "이미 탈퇴한 회원입니다."),
    NOT_SUBSCRIBED(HttpStatus.BAD_REQUEST, "MEMBER-008", "구독 중이 아닌 회원입니다."),
    EXPIRED_PRIME_MEMBERSHIP(HttpStatus.BAD_REQUEST, "MEMBER-009", "만료된 프라임 멤버십입니다."),
    ALREADY_SELLER(HttpStatus.BAD_REQUEST, "MEMBER-010", "이미 판매자 회원입니다."),
    NOT_SELLER(HttpStatus.BAD_REQUEST, "MEMBER-011", "판매자 권한이 없습니다."),

    // Auth Errors
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH-001", "인증이 필요합니다."),

    // Order Errors
    INVALID_TOTAL_AMOUNT(HttpStatus.BAD_REQUEST, "ORDER-001", "유효하지 않은 총 주문 금액입니다."),
    INVALID_CANCEL_STATUS(HttpStatus.BAD_REQUEST, "ORDER-002", "취소할 수 없는 주문 상태입니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "ORDER-003", "유효하지 않은 주문 상태 전이입니다."),
    INVALID_ORDER_QUANTITY(HttpStatus.BAD_REQUEST, "ORDER-004", "주문 수량은 0보다 커야 합니다."),
    INVALID_ORDER_PRICE(HttpStatus.BAD_REQUEST, "ORDER-005", "주문 가격은 0 이상이어야 합니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER-006", "주문을 찾을 수 없습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "ORDER-007", "유효하지 않은 주문 상태입니다."),
    ORDER_NOT_OWNER(HttpStatus.BAD_REQUEST, "ORDER-008", "주문의 소유자가 아닙니다."),

    // Payment Errors
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "PAYMENT-001", "유효하지 않은 결제 금액입니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT-002", "결제를 찾을 수 없습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "PAYMENT-003", "유효하지 않은 결제 상태입니다."),

    // Queue Errors
    QUEUE_FULL(HttpStatus.SERVICE_UNAVAILABLE, "QUEUE-001", "대기열이 가득 찼습니다. 잠시 후 다시 시도해주세요."),
    QUEUE_POSITION_NOT_FOUND(HttpStatus.NOT_FOUND, "QUEUE-002", "대기열 위치를 찾을 수 없습니다."),
    QUEUE_ALREADY_IN(HttpStatus.BAD_REQUEST, "QUEUE-003", "이미 대기열에 있는 사용자입니다."),
    QUEUE_ADD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "QUEUE-004", "대기열 추가에 실패했습니다."),
    QUEUE_TIMEOUT(HttpStatus.GONE, "QUEUE-005", "대기 시간이 초과되었습니다."),
    QUEUE_INVALID_ACCESS(HttpStatus.BAD_REQUEST, "QUEUE-006", "잘못된 대기열 접근입니다."),
    QUEUE_ENTERED(HttpStatus.ACCEPTED, "QUEUE-008", "현재 주문이 많아 대기열에 등록되었습니다. 잠시만 기다려주세요."),
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
