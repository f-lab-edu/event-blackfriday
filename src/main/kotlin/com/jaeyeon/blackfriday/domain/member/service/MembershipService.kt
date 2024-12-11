package com.jaeyeon.blackfriday.domain.member.service

import com.jaeyeon.blackfriday.common.global.MemberException
import com.jaeyeon.blackfriday.common.security.session.SessionConstants.USER_KEY
import com.jaeyeon.blackfriday.common.security.session.SessionUser
import com.jaeyeon.blackfriday.domain.member.domain.Member
import com.jaeyeon.blackfriday.domain.member.domain.enum.MembershipType
import com.jaeyeon.blackfriday.domain.member.dto.MemberResponse
import com.jaeyeon.blackfriday.domain.member.repository.MemberRepository
import jakarta.servlet.http.HttpSession
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class MembershipService(
    private val memberRepository: MemberRepository,
    private val httpSession: HttpSession,
) {

    @Transactional(readOnly = true)
    fun checkPrimeMembership(member: Member): MemberResponse {
        val currentMember = memberRepository.findByIdOrNull(member.id)
            ?: throw MemberException.notFound()

        if (isMembershipExpired(currentMember)) {
            handleMembershipExpiration(currentMember)
            throw MemberException.expiredPrimeMembership()
        }

        return MemberResponse.from(currentMember)
    }

    fun updateToPrime(member: Member): MemberResponse {
        val currentMember = memberRepository.findByIdOrNull(member.id)
            ?: throw MemberException.notFound()

        if (currentMember.membershipType == MembershipType.PRIME) {
            throw MemberException.alreadySubscribed()
        }

        currentMember.upgradeToPrime()
        val savedMember = memberRepository.save(currentMember)

        httpSession.setAttribute(USER_KEY, SessionUser.from(savedMember))
        return MemberResponse.from(savedMember)
    }

    fun downgradeToNormal(member: Member): MemberResponse {
        val currentMember = memberRepository.findByIdOrNull(member.id)
            ?: throw MemberException.notFound()

        if (currentMember.membershipType != MembershipType.PRIME) {
            throw MemberException.notSubscribed()
        }

        currentMember.downgradeToNormal()
        val savedMember = memberRepository.save(currentMember)

        httpSession.setAttribute(USER_KEY, SessionUser.from(savedMember))

        return MemberResponse.from(savedMember)
    }

    private fun handleMembershipExpiration(member: Member) {
        member.downgradeToNormal()
        val savedMember = memberRepository.save(member)
        httpSession.setAttribute(USER_KEY, SessionUser.from(savedMember))
    }

    private fun isMembershipExpired(member: Member): Boolean =
        member.membershipType == MembershipType.PRIME &&
            member.membershipEndDate?.isBefore(LocalDateTime.now()) == true
}
