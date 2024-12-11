package com.jaeyeon.blackfriday.common.security.session

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.jaeyeon.blackfriday.domain.member.domain.Member
import java.io.Serializable
import java.time.LocalDateTime

object SessionConstants {
    const val USER_KEY = "USER"
}

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
data class SessionUser(
    val id: Long,
    val email: String,
    val roles: Set<String>,
    @JsonSerialize(using = LocalDateTimeSerializer::class)
    @JsonDeserialize(using = LocalDateTimeDeserializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) : Serializable {
    companion object {
        private const val serialVersionUID = 1L

        fun from(member: Member) = SessionUser(
            id = member.id!!,
            email = member.email,
            roles = member.membershipType.getRoleNames(),
            createdAt = LocalDateTime.now(),
        )
    }
}
