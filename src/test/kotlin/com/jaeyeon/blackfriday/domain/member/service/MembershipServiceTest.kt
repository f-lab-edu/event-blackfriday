package com.jaeyeon.blackfriday.domain.member.service
import com.jaeyeon.blackfriday.common.global.MemberException
import com.jaeyeon.blackfriday.common.security.session.SessionConstants.USER_KEY
import com.jaeyeon.blackfriday.domain.member.domain.Member
import com.jaeyeon.blackfriday.domain.member.domain.enum.MembershipType
import com.jaeyeon.blackfriday.domain.member.repository.MemberRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyAll
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.repository.findByIdOrNull
import java.time.LocalDateTime

@ExtendWith(MockKExtension::class)
class MembershipServiceTest : BehaviorSpec({
    val memberRepository = mockk<MemberRepository>()
    val httpSession = mockk<HttpSession>()
    val membershipService = MembershipService(memberRepository, httpSession)

    Given("멤버십 서비스 테스트") {
        val memberId = 1L
        val testMember = Member(
            id = memberId,
            email = "test@example.com",
            password = "password",
            name = "Test User",
            membershipType = MembershipType.NORMAL,
        )

        When("일반 회원의 멤버십 상태를 확인할 때") {
            every { memberRepository.findByIdOrNull(memberId) } returns testMember

            Then("현재 멤버십 상태가 정상적으로 반환된다") {
                val result = membershipService.checkPrimeMembership(testMember)
                result.membershipType shouldBe MembershipType.NORMAL
                verifyAll {
                    memberRepository.findByIdOrNull(memberId)
                }
            }
        }

        When("만료된 프라임 멤버십을 확인할 때") {
            val expiredMember = Member(
                id = memberId,
                email = "test@example.com",
                password = "password",
                name = "Test User",
                membershipType = MembershipType.PRIME,
                membershipStartDate = LocalDateTime.now().minusDays(2),
                membershipEndDate = LocalDateTime.now().minusDays(1),
            )

            every { memberRepository.findByIdOrNull(memberId) } returns expiredMember
            every { memberRepository.save(any()) } returns expiredMember
            every { httpSession.setAttribute(USER_KEY, any()) } just runs

            Then("예외가 발생하고 멤버십이 일반으로 변경된다") {
                shouldThrow<MemberException> {
                    membershipService.checkPrimeMembership(expiredMember)
                }

                verifyAll {
                    memberRepository.findByIdOrNull(memberId)
                    memberRepository.save(any())
                    httpSession.setAttribute(USER_KEY, any())
                }
            }
        }

        When("일반 회원이 프라임으로 업그레이드할 때") {
            val updatedMember = Member(
                id = memberId,
                email = "test@example.com",
                password = "password",
                name = "Test User",
                membershipType = MembershipType.PRIME,
                membershipStartDate = LocalDateTime.now(),
                membershipEndDate = LocalDateTime.now().plusYears(1),
            )

            every { memberRepository.findByIdOrNull(memberId) } returns testMember
            every { memberRepository.save(any()) } returns updatedMember
            every { httpSession.setAttribute(USER_KEY, any()) } just runs

            Then("멤버십이 프라임으로 업그레이드된다.") {
                val result = membershipService.updateToPrime(testMember)
                result.membershipType shouldBe MembershipType.PRIME
                verify {
                    memberRepository.findByIdOrNull(memberId)
                    memberRepository.save(any())
                    httpSession.setAttribute(any(), any())
                }
            }
        }

        When("프라임 회원이 일반으로 다운그레이드할 때") {
            val primeMember = Member(
                id = memberId,
                email = "test@example.com",
                password = "password",
                name = "Test User",
                membershipType = MembershipType.PRIME,
                membershipStartDate = LocalDateTime.now(),
                membershipEndDate = LocalDateTime.now().plusYears(1),
            )

            val downgradedMember = Member(
                id = memberId,
                email = "test@example.com",
                password = "password",
                name = "Test User",
                membershipType = MembershipType.NORMAL,
                membershipStartDate = null,
                membershipEndDate = null,
            )

            every { memberRepository.findByIdOrNull(memberId) } returns primeMember
            every { memberRepository.save(any()) } returns downgradedMember
            every { httpSession.setAttribute(any(), any()) } just runs

            Then("멤버십이 일반으로 다운그레이드된다") {
                val result = membershipService.downgradeToNormal(primeMember)
                result.membershipType shouldBe MembershipType.NORMAL
                verify {
                    memberRepository.findByIdOrNull(memberId)
                    memberRepository.save(any())
                    httpSession.setAttribute(any(), any())
                }
            }
        }
    }
})
