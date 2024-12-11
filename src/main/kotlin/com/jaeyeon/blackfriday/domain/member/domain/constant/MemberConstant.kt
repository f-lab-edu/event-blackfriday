package com.jaeyeon.blackfriday.domain.member.domain.constant

object MemberConstant {
    val EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$".toRegex()
    const val PASSWORD_MIN_LENGTH = 8
    const val PASSWORD_MAX_LENGTH = 60
    const val NAME_MAX_LENGTH = 20
}
