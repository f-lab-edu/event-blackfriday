package com.jaeyeon.blackfriday.domain.member.controller

import com.jaeyeon.blackfriday.common.security.annotation.CurrentUser
import com.jaeyeon.blackfriday.common.security.annotation.LoginRequired
import com.jaeyeon.blackfriday.common.security.annotation.PrimeOnly
import com.jaeyeon.blackfriday.domain.member.domain.Member
import com.jaeyeon.blackfriday.domain.member.dto.MemberResponse
import com.jaeyeon.blackfriday.domain.member.service.MembershipService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Membership", description = "멤버십 API")
@RestController
@RequestMapping("/api/v1/memberships")
class MembershipController(
    private val membershipService: MembershipService,
) {

    @Operation(
        summary = "Prime 멤버십 가입",
        description = "일반 회원을 Prime 멤버십으로 업그레이드합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "멤버십 가입 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "400", description = "이미 Prime 멤버십 회원"),
        ],
    )
    @LoginRequired
    @PostMapping("/subscribe")
    fun upgradeToPrime(@CurrentUser member: Member): ResponseEntity<MemberResponse> {
        return ResponseEntity.ok(membershipService.updateToPrime(member))
    }

    @Operation(
        summary = "Prime 멤버십 해지",
        description = "Prime 멤버십을 해지하고 일반 회원으로 전환합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "멤버십 해지 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
            ApiResponse(responseCode = "403", description = "Prime 멤버십 회원이 아님"),
        ],
    )
    @PrimeOnly
    @LoginRequired
    @PostMapping("/unsubscribe")
    fun downgradeToNormal(@CurrentUser member: Member): ResponseEntity<MemberResponse> {
        return ResponseEntity.ok(membershipService.downgradeToNormal(member))
    }

    @Operation(
        summary = "멤버십 상태 확인",
        description = "현재 회원의 멤버십 상태를 확인합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        ],
    )
    @LoginRequired
    @GetMapping("/status")
    fun checkMembershipStatus(@CurrentUser member: Member): ResponseEntity<MemberResponse> {
        val membershipStatus = membershipService.checkPrimeMembership(member)
        return ResponseEntity.ok(membershipStatus)
    }
}
