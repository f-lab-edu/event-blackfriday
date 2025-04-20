package com.jaeyeon.blackfriday.common.security.session

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.jaeyeon.blackfriday.domain.member.domain.Member
import com.jaeyeon.blackfriday.domain.member.domain.enum.MembershipType
import org.slf4j.LoggerFactory
import java.time.LocalDateTime

object SecurityConstants {
    const val AUTH_HEADER = "X-Auth-Token"
}

object SessionConstants {
    const val USER_KEY = "USER"
    const val SESSION_USER_ATTRIBUTE = "SESSION_USER"
}

object Roles {
    const val PRIME = "ROLE_PRIME"
    const val SELLER = "ROLE_SELLER"
}

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
    private val log = LoggerFactory.getLogger(javaClass)

    @JsonCreator
    constructor() : this(
        id = 0L,
        email = "",
        name = "",
        membershipType = MembershipType.NORMAL,
        roles = emptySet(),
    ) {
        log.debug("[SessionUser] Default constructor called during deserialization")
    }

    companion object {
        private val log = LoggerFactory.getLogger(SessionUser::class.java)

        fun from(member: Member): SessionUser {
            val sessionUser = SessionUser(
                id = member.id!!,
                email = member.email,
                name = member.name,
                membershipType = member.membershipType,
                roles = member.membershipType.getRoleNames(),
            )
            log.debug(
                "[SessionUser] Created from Member: id={}, email={}",
                sessionUser.id,
                sessionUser.email,
            )
            return sessionUser
        }
    }
}
