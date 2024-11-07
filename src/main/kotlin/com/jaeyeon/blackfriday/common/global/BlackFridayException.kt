package com.jaeyeon.blackfriday.common.global

import com.jaeyeon.blackfriday.common.exception.ErrorCode

open class BlackFridayException(
    val errorCode: ErrorCode,
    message: String? = errorCode.message,
) : RuntimeException(message)
