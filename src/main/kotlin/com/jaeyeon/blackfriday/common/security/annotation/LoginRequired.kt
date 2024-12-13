package com.jaeyeon.blackfriday.common.security.annotation

import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.FUNCTION
import kotlin.annotation.AnnotationTarget.VALUE_PARAMETER

@Target(FUNCTION, VALUE_PARAMETER)
@Retention(RUNTIME)
annotation class LoginRequired
