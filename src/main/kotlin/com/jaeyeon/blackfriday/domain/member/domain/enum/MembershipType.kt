package com.jaeyeon.blackfriday.domain.member.domain.enum

enum class MembershipType(
    val description: String,
) {
    NORMAL("일반 회원"),
    PRIME("프라임 회원"),
    SELLER("판매자"),
    ;

    fun getRoleNames(): Set<String> = when (this) {
        NORMAL -> setOf("ROLE_USER")
        PRIME -> setOf("ROLE_USER", "ROLE_PRIME")
        SELLER -> setOf("ROLE_USER", "ROLE_SELLER")
    }
}
