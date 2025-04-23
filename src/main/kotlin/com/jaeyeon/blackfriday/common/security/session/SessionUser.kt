package com.jaeyeon.blackfriday.common.security.session

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.jaeyeon.blackfriday.domain.member.domain.Member
import com.jaeyeon.blackfriday.domain.member.domain.enum.MembershipType
import java.time.LocalDateTime

object SecurityConstants {
    const val AUTH_HEADER = "X-Auth-Token"
}

object SessionConstants {
    const val USER_KEY = "USER"
}

object Roles {
    const val PRIME = "ROLE_PRIME"
    const val SELLER = "ROLE_SELLER"
}

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
data class SessionUser(
    val id: Long,
    val email: String,
    val name: String,
    val membershipType: MembershipType,
    val roles: Set<String>,
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    companion object {
        fun from(member: Member) = SessionUser(
            id = member.id!!,
            email = member.email,
            name = member.name,
            membershipType = member.membershipType,
            roles = member.membershipType.getRoleNames(),
            createdAt = LocalDateTime.now(),
        )
    }
}
