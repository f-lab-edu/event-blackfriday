package com.jaeyeon.blackfriday.domain.member.domain

import com.jaeyeon.blackfriday.common.global.MemberException
import com.jaeyeon.blackfriday.common.model.BaseTimeEntity
import com.jaeyeon.blackfriday.domain.member.domain.constant.MemberConstant.EMAIL_REGEX
import com.jaeyeon.blackfriday.domain.member.domain.constant.MemberConstant.NAME_MAX_LENGTH
import com.jaeyeon.blackfriday.domain.member.domain.constant.MemberConstant.PASSWORD_MAX_LENGTH
import com.jaeyeon.blackfriday.domain.member.domain.constant.MemberConstant.PASSWORD_MIN_LENGTH
import com.jaeyeon.blackfriday.domain.member.domain.enum.MembershipType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.SQLRestriction
import java.time.LocalDateTime

@Entity
@Table(name = "members")
@SQLRestriction("is_deleted = false")
class Member(

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(unique = true, nullable = false, length = 255)
    val email: String,

    @Column(nullable = false, length = 60)
    var password: String,

    @Column(nullable = false, length = 20)
    var name: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var membershipType: MembershipType = MembershipType.NORMAL,

    @Column
    var membershipStartDate: LocalDateTime? = null,

    @Column
    var membershipEndDate: LocalDateTime? = null,

    @Column(nullable = false)
    var isDeleted: Boolean = false,
) : BaseTimeEntity() {

    init {
        validateEmail(email)
        validateName(name)
    }

    private fun validateEmail(email: String) {
        if (!email.matches(EMAIL_REGEX)) {
            throw MemberException.invalidEmail()
        }
    }

    private fun validatePassword(password: String) {
        if (password.length !in PASSWORD_MIN_LENGTH..PASSWORD_MAX_LENGTH) {
            throw MemberException.invalidPassword()
        }
    }

    private fun validateName(name: String) {
        if (name.isBlank() || name.length > NAME_MAX_LENGTH) {
            throw MemberException.invalidName()
        }
    }

    fun updateName(newName: String) {
        validateName(newName)
        this.name = newName
    }

    fun updatePassword(newPassword: String) {
        validatePassword(newPassword)
        this.password = newPassword
    }

    fun withdraw() {
        validateWithdrawal()
        this.isDeleted = true
        this.membershipType = MembershipType.NORMAL
        this.membershipStartDate = null
        this.membershipEndDate = null
    }

    private fun validateWithdrawal() {
        if (isDeleted) {
            throw MemberException.alreadyWithdrawn()
        }
    }

    fun upgradeToPrime() {
        validatePrimeUpgrade()
        this.membershipType = MembershipType.PRIME
        this.membershipStartDate = LocalDateTime.now()
        this.membershipEndDate = LocalDateTime.now().plusYears(1)
    }

    fun downgradeToNormal() {
        validatePrimeDowngrade()
        this.membershipType = MembershipType.NORMAL
        this.membershipStartDate = null
        this.membershipEndDate = null
    }

    fun upgradeToSeller() {
        validateSellerUpgrade()
        this.membershipType = MembershipType.SELLER
    }

    private fun validateSellerUpgrade() {
        if (membershipType == MembershipType.SELLER) {
            throw MemberException.alreadySeller()
        }
    }

    private fun validatePrimeUpgrade() {
        if (membershipType == MembershipType.PRIME) {
            throw MemberException.alreadySubscribed()
        }
    }

    private fun validatePrimeDowngrade() {
        if (membershipType == MembershipType.NORMAL) {
            throw MemberException.notSubscribed()
        }
    }
}
