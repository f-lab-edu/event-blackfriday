package com.jaeyeon.blackfriday.domain.member.service

import com.jaeyeon.blackfriday.common.global.MemberException
import com.jaeyeon.blackfriday.common.security.session.SessionConstants.USER_KEY
import com.jaeyeon.blackfriday.common.security.session.SessionUser
import com.jaeyeon.blackfriday.domain.member.dto.LoginRequest
import com.jaeyeon.blackfriday.domain.member.dto.MemberResponse
import com.jaeyeon.blackfriday.domain.member.dto.SignUpRequest
import com.jaeyeon.blackfriday.domain.member.dto.UpdateMemberRequest
import com.jaeyeon.blackfriday.domain.member.repository.MemberRepository
import jakarta.servlet.http.HttpSession
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository,
    private val passwordEncoder: PasswordEncoder,
    private val httpSession: HttpSession,
) {

    fun signUp(request: SignUpRequest): MemberResponse {
        if (memberRepository.existsByEmail(request.email)) {
            throw MemberException.alreadyExists()
        }

        val encodedPassword = passwordEncoder.encode(request.password)
        val member = request.toEntity(encodedPassword)
        return MemberResponse.from(memberRepository.save(member))
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): MemberResponse {
        if (httpSession.getAttribute(USER_KEY) != null) {
            httpSession.invalidate()
        }

        val member = memberRepository.findByEmail(request.email)
            ?: throw MemberException.notFound()

        if (!passwordEncoder.matches(request.password, member.password)) {
            throw MemberException.invalidPassword()
        }

        httpSession.setAttribute(USER_KEY, SessionUser.from(member))
        return MemberResponse.from(member)
    }

    fun logout() {
        httpSession.invalidate()
    }

    @Transactional(readOnly = true)
    fun getMyInfo(memberId: Long): MemberResponse {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw MemberException.notFound()
        return MemberResponse.from(member)
    }

    fun updateMember(memberId: Long, request: UpdateMemberRequest): MemberResponse {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw MemberException.notFound()

        request.name?.let { member.updateName(it) }
        request.password?.let {
            member.updatePassword(passwordEncoder.encode(it))
        }

        return MemberResponse.from(member)
    }

    fun withdraw(memberId: Long) {
        val member = memberRepository.findByIdOrNull(memberId)
            ?: throw MemberException.notFound()

        member.withdraw()
        memberRepository.save(member)
        httpSession.invalidate()
    }
}
