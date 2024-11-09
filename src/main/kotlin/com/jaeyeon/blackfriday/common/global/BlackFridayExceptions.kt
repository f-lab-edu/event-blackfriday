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
    }
}

class CategoryException(
    errorCode: ErrorCode,
    message: String? = errorCode.message,
) : BlackFridayException(errorCode, message) {

    companion object {
        fun invalidName() = CategoryException(ErrorCode.INVALID_CATEGORY_NAME)
        fun invalidDepth() = CategoryException(ErrorCode.INVALID_CATEGORY_DEPTH)
        fun invalidDiscountRate() = CategoryException(ErrorCode.INVALID_CATEGORY_DISCOUNT_RATE)
    }
}
