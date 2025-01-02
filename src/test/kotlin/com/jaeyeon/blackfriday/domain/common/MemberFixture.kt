package com.jaeyeon.blackfriday.domain.common

import com.jaeyeon.blackfriday.domain.member.domain.Member
import com.jaeyeon.blackfriday.domain.member.domain.enum.MembershipType

object MemberFixture {
    private object DefaultValues {
        const val ID = 1L
        const val EMAIL = "test@example.com"
        const val PASSWORD = "password123!"
        const val NAME = "테스트 사용자"
    }

    fun createSeller(
        id: Long = DefaultValues.ID,
        email: String = DefaultValues.EMAIL,
        password: String = DefaultValues.PASSWORD,
        name: String = DefaultValues.NAME,
    ) = Member(
        id = id,
        email = email,
        password = password,
        name = name,
        membershipType = MembershipType.SELLER,
    )
}
