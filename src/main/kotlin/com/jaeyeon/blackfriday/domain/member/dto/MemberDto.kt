package com.jaeyeon.blackfriday.domain.member.dto

import com.jaeyeon.blackfriday.domain.member.domain.Member
import com.jaeyeon.blackfriday.domain.member.domain.enum.MembershipType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.LocalDateTime

data class SignUpRequest(
    @Schema(description = "이메일", example = "test@example.com")
    @field:Email(message = "유효한 이메일 형식이어야 합니다.")
    @field:NotBlank(message = "이메일은 필수입니다.")
    val email: String,

    @Schema(description = "비밀번호 (8-20자의 영문, 숫자, 특수문자 포함)", example = "Password123!")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#\$%^&*]).{8,20}\$",
        message = "비밀번호는 8-60자의 영문, 숫자, 특수문자를 포함해야 합니다",
    )
    val password: String,

    @Schema(description = "이름", example = "홍길동")
    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,
) {
    fun toEntity(encodedPassword: String): Member {
        return Member(
            email = email,
            password = encodedPassword,
            name = name,
        )
    }
}

data class LoginRequest(
    @Schema(description = "이메일", example = "test@example.com")
    @field:Email(message = "유효한 이메일 형식이어야 합니다")
    @field:NotBlank(message = "이메일은 필수입니다")
    val email: String,

    @Schema(description = "비밀번호", example = "Password123!")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#\$%^&*]).{8,20}\$",
        message = "비밀번호는 8-60자의 영문, 숫자, 특수문자를 포함해야 합니다",
    )
    val password: String,
)

data class UpdateMemberRequest(
    @Schema(description = "변경할 이름", example = "홍길동")
    val name: String? = null,

    @Schema(description = "변경할 비밀번호", example = "NewPassword123!")
    val password: String? = null,
)

data class MemberResponse(
    @Schema(description = "회원 ID")
    val id: Long,

    @Schema(description = "이메일")
    val email: String,

    @Schema(description = "이름")
    val name: String,

    @Schema(description = "멤버십 타입")
    val membershipType: MembershipType,

    @Schema(description = "멤버십 시작일")
    val membershipStartDate: LocalDateTime?,

    @Schema(description = "멤버십 종료일")
    val membershipEndDate: LocalDateTime?,
) {
    companion object {
        fun from(member: Member) = MemberResponse(
            id = member.id!!,
            email = member.email,
            name = member.name,
            membershipType = member.membershipType,
            membershipStartDate = member.membershipStartDate,
            membershipEndDate = member.membershipEndDate,
        )
    }
}
