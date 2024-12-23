package com.jaeyeon.blackfriday.domain.member.service

import com.jaeyeon.blackfriday.common.security.session.SessionConstants.USER_KEY
import com.jaeyeon.blackfriday.domain.member.domain.Member
import com.jaeyeon.blackfriday.domain.member.dto.LoginRequest
import com.jaeyeon.blackfriday.domain.member.dto.SignUpRequest
import com.jaeyeon.blackfriday.domain.member.dto.UpdateMemberRequest
import com.jaeyeon.blackfriday.domain.member.repository.MemberRepository
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import jakarta.servlet.http.HttpSession
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder

@ExtendWith(MockitoExtension::class)
class MemberServiceTest : BehaviorSpec({
    val memberRepository = mockk<MemberRepository>()
    val passwordEncoder = mockk<PasswordEncoder>()
    val httpSession = mockk<HttpSession>()
    val memberService = MemberService(memberRepository, passwordEncoder, httpSession)

    Given("회원 서비스 테스트") {
        val email = "test@example.com"
        val password = "Test1234!"
        val encodedPassword = "encodedPassword"
        val name = "TestUser"
        val member = Member(
            id = 1L,
            email = email,
            password = encodedPassword,
            name = name,
        )

        When("회원가입을 시도할 때") {
            val request = SignUpRequest(email, password, name)

            every { memberRepository.existsByEmail(any()) } returns false
            every { passwordEncoder.encode(any()) } returns encodedPassword
            every { memberRepository.save(any()) } returns member

            Then("회원가입이 성공한다.") {
                val result = memberService.signUp(request)

                result.email shouldBe email
                result.name shouldBe name

                verify { memberRepository.existsByEmail(email) }
                verify { passwordEncoder.encode(password) }
                verify { memberRepository.save(any()) }
            }
        }

        When("로그인을 시도할 때") {
            val request = LoginRequest(email, password)

            every { httpSession.getAttribute(USER_KEY) } returns null
            every { memberRepository.findByEmail(any()) } returns member
            every { passwordEncoder.matches(any(), any()) } returns true
            every { httpSession.setAttribute(any(), any()) } just runs

            Then("로그인이 성공한다") {
                val result = memberService.login(request)

                result.email shouldBe email
                result.name shouldBe name

                verify { httpSession.getAttribute(USER_KEY) }
                verify { memberRepository.findByEmail(email) }
                verify { passwordEncoder.matches(password, encodedPassword) }
                verify { httpSession.setAttribute(USER_KEY, any()) }
            }
        }

        When("로그아웃을 시도할 때") {
            every { httpSession.invalidate() } just runs

            Then("로그아웃이 성공한다") {
                shouldNotThrow<Exception> {
                    memberService.logout()
                }

                verify { httpSession.invalidate() }
            }
        }

        When("내 정보를 조회할 때") {
            val memberId = 1L

            every { memberRepository.findByIdOrNull(memberId) } returns member

            Then("회원 정보가 조회된다") {
                val result = memberService.getMyInfo(memberId)

                result.email shouldBe email
                result.name shouldBe name

                verify { memberRepository.findByIdOrNull(memberId) }
            }
        }

        When("회원 정보를 수정할 때") {
            val memberId = 1L
            val newName = "NewName"
            val request = UpdateMemberRequest(name = newName)

            every { memberRepository.findByIdOrNull(memberId) } returns member

            Then("회원 정보가 수정된다") {
                val result = memberService.updateMember(memberId, request)
                result.name shouldBe newName
                verify { memberRepository.findByIdOrNull(memberId) }
            }
        }

        When("회원 탈퇴를 시도할 때") {
            val memberId = 1L

            every { memberRepository.findByIdOrNull(memberId) } returns member
            every { memberRepository.save(any()) } returns member
            every { httpSession.invalidate() } just runs

            Then("회원 탈퇴가 성공한다") {
                shouldNotThrow<Exception> {
                    memberService.withdraw(memberId)
                }

                verify { memberRepository.findByIdOrNull(memberId) }
                verify { memberRepository.save(any()) }
                verify { httpSession.invalidate() }
            }
        }
    }
})
