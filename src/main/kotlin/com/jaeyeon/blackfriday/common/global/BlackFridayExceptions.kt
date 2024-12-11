package com.jaeyeon.blackfriday.common.global

import com.jaeyeon.blackfriday.common.exception.ErrorCode

open class BlackFridayException(
    val errorCode: ErrorCode,
    message: String? = errorCode.message,
) : RuntimeException(message)

class ProductException(
    errorCode: ErrorCode,
    message: String? = errorCode.message,
) : BlackFridayException(errorCode, message) {

    companion object {
        fun invalidName(message: String? = null) = ProductException(ErrorCode.INVALID_PRODUCT_NAME, message)
        fun invalidDescription(message: String? = null) = ProductException(
            ErrorCode.INVALID_PRODUCT_DESCRIPTION,
            message,
        )
        fun invalidPrice(message: String? = null) = ProductException(ErrorCode.INVALID_PRODUCT_PRICE, message)
        fun invalidStock(message: String? = null) = ProductException(ErrorCode.INVALID_PRODUCT_STOCK_QUANTITY, message)
        fun outOfStock(message: String? = null) = ProductException(ErrorCode.NOT_ENOUGH_STOCK, message)
        fun invalidProductNotFound(message: String? = null) = ProductException(
            ErrorCode.INVALID_PRODUCT_NOT_FOUND,
            message,
        )
    }
}

class CategoryException(
    errorCode: ErrorCode,
    message: String? = errorCode.message,
) : BlackFridayException(errorCode, message) {

    companion object {
        fun invalidName() = CategoryException(ErrorCode.INVALID_CATEGORY_NAME)
        fun invalidDepth() = CategoryException(ErrorCode.INVALID_CATEGORY_DEPTH)
        fun invalidDisplayOrder() = CategoryException(ErrorCode.INVALID_CATEGORY_DISPLAY_ORDER)
        fun invalidClosureDepth() = CategoryException(ErrorCode.INVALID_CATEGORY_CLOSURE_DEPTH)
        fun invalidNotFound() = CategoryException(ErrorCode.INVALID_CATEGORY_NOT_FOUND)
        fun invalidDuplicateName() = CategoryException(ErrorCode.INVALID_CATEGORY_DUPLICATE_NAME)
    }
}

class MemberException(
    errorCode: ErrorCode,
    message: String? = errorCode.message,
) : BlackFridayException(errorCode, message) {

    companion object {
        fun invalidEmail() = MemberException(ErrorCode.INVALID_MEMBER_EMAIL)
        fun invalidPassword() = MemberException(ErrorCode.INVALID_MEMBER_PASSWORD)
        fun invalidName() = MemberException(ErrorCode.INVALID_MEMBER_NAME)
        fun notFound() = MemberException(ErrorCode.MEMBER_NOT_FOUND)
        fun alreadyExists() = MemberException(ErrorCode.MEMBER_ALREADY_EXISTS)
        fun alreadySubscribed() = MemberException(ErrorCode.ALREADY_SUBSCRIBED)
        fun alreadyWithdrawn() = MemberException(ErrorCode.ALREADY_WITHDRAWN)
        fun unauthorized() = MemberException(ErrorCode.UNAUTHORIZED)
        fun notSubscribed() = MemberException(ErrorCode.NOT_SUBSCRIBED)
        fun expiredPrimeMembership() = MemberException(ErrorCode.EXPIRED_PRIME_MEMBERSHIP)
    }
}
