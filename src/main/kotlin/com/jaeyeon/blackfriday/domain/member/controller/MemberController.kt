package com.jaeyeon.blackfriday.domain.member.controller

import com.jaeyeon.blackfriday.common.security.annotation.CurrentUser
import com.jaeyeon.blackfriday.common.security.annotation.LoginRequired
import com.jaeyeon.blackfriday.domain.member.domain.Member
import com.jaeyeon.blackfriday.domain.member.dto.LoginRequest
import com.jaeyeon.blackfriday.domain.member.dto.MemberResponse
import com.jaeyeon.blackfriday.domain.member.dto.SignUpRequest
import com.jaeyeon.blackfriday.domain.member.dto.UpdateMemberRequest
import com.jaeyeon.blackfriday.domain.member.service.MemberService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/api/v1/members")
class MemberController(
    private val memberService: MemberService,
) {
    @Operation(
        summary = "회원 가입",
        description = "새로운 회원을 등록합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "회원 가입 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "409", description = "이미 존재하는 이메일"),
        ],
    )
    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ResponseEntity<MemberResponse> {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(memberService.signUp(request))
    }

    @Operation(
        summary = "로그인",
        description = "이메일과 비밀번호로 로그인합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "로그인 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증 실패"),
        ],
    )
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<MemberResponse> {
        val response = memberService.login(request)
        return ResponseEntity.ok(response)
    }

    @Operation(
        summary = "내 프로필 조회",
        description = "로그인한 회원의 프로필을 조회합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        ],
    )
    @LoginRequired
    @GetMapping("/profiles/me")
    fun getMyProfile(@CurrentUser member: Member): ResponseEntity<MemberResponse> {
        return ResponseEntity.ok(memberService.getMyInfo(member))
    }

    @Operation(
        summary = "프로필 수정",
        description = "로그인한 회원의 프로필을 수정합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "수정 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        ],
    )
    @LoginRequired
    @PutMapping("/profiles/me/details")
    fun updateProfileDetails(
        @CurrentUser member: Member,
        @Valid @RequestBody request: UpdateMemberRequest,
    ): ResponseEntity<MemberResponse> {
        return ResponseEntity.ok(memberService.updateMember(member, request))
    }

    @Operation(
        summary = "로그아웃",
        description = "현재 로그인된 세션을 종료합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "로그아웃 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        ],
    )
    @LoginRequired
    @PostMapping("/logout")
    fun logout(): ResponseEntity<Unit> {
        memberService.logout()
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "회원 탈퇴",
        description = "로그인한 회원의 계정을 삭제합니다.",
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "탈퇴 성공"),
            ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
        ],
    )
    @LoginRequired
    @DeleteMapping("/accounts/me")
    fun deleteMyAccount(@CurrentUser member: Member): ResponseEntity<Unit> {
        memberService.withdraw(member)
        return ResponseEntity.noContent().build()
    }
}
