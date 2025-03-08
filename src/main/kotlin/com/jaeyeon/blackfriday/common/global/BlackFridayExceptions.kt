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
        fun notOwner() = ProductException(ErrorCode.PRODUCT_NOT_OWNER)
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
        fun notOwner() = CategoryException(ErrorCode.CATEGORY_NOT_OWNER)
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
        fun alreadySeller() = MemberException(ErrorCode.ALREADY_SELLER)
        fun notSeller() = MemberException(ErrorCode.NOT_SELLER)
    }
}

class OrderException(
    errorCode: ErrorCode,
    message: String? = errorCode.message,
) : BlackFridayException(errorCode, message) {

    companion object {
        fun invalidTotalAmount() = OrderException(ErrorCode.INVALID_TOTAL_AMOUNT)
        fun invalidCancelStatus() = OrderException(ErrorCode.INVALID_CANCEL_STATUS)
        fun invalidStatusTransition() = OrderException(ErrorCode.INVALID_STATUS_TRANSITION)
        fun invalidOrderQuantity() = OrderException(ErrorCode.INVALID_ORDER_QUANTITY)
        fun invalidOrderPrice() = OrderException(ErrorCode.INVALID_ORDER_PRICE)
        fun orderNotFound() = OrderException(ErrorCode.ORDER_NOT_FOUND)
        fun invalidOrderStatus() = OrderException(ErrorCode.INVALID_ORDER_STATUS)
        fun invalidProductName() = OrderException(ErrorCode.INVALID_PRODUCT_NAME)
        fun notOwner() = OrderException(ErrorCode.ORDER_NOT_OWNER)
        fun invalidProductPrice() = OrderException(ErrorCode.INVALID_PRODUCT_PRICE)
    }
}

class PaymentException(
    errorCode: ErrorCode,
    message: String? = errorCode.message,
) : BlackFridayException(errorCode, message) {

    companion object {
        fun invalidPaymentAmount() = PaymentException(ErrorCode.INVALID_PAYMENT_AMOUNT)
        fun paymentNotFound() = PaymentException(ErrorCode.PAYMENT_NOT_FOUND)
        fun invalidPaymentStatus() = PaymentException(ErrorCode.INVALID_PAYMENT_STATUS)
        fun notPaymentOwner() = PaymentException(ErrorCode.ORDER_NOT_OWNER)
    }
}

class OrderQueueException(
    errorCode: ErrorCode,
    message: String? = errorCode.message,
) : BlackFridayException(errorCode, message) {

    companion object {
        fun queueFull() = OrderQueueException(ErrorCode.QUEUE_FULL)
        fun alreadyInQueue() = OrderQueueException(ErrorCode.QUEUE_ALREADY_IN)
        fun failedToAddToQueue() = OrderQueueException(ErrorCode.QUEUE_ADD_FAILED)
        fun queuePositionNotFound() = OrderQueueException(ErrorCode.QUEUE_POSITION_NOT_FOUND)
        fun timeout() = OrderQueueException(ErrorCode.QUEUE_TIMEOUT)
        fun invalidAccess() = OrderQueueException(ErrorCode.QUEUE_INVALID_ACCESS)
    }
}
