package com.jaeyeon.blackfriday.domain.payment.repository

import com.jaeyeon.blackfriday.domain.payment.domain.Payment
import com.jaeyeon.blackfriday.domain.payment.domain.enum.PaymentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PaymentRepository : JpaRepository<Payment, Long> {
    fun findByPaymentNumber(paymentNumber: String): Payment?
    fun findByOrderNumber(orderNumber: String): Payment?

    @Query(
        """
        SELECT p FROM Payment p
        WHERE p.memberId = :memberId
        AND (:status IS NULL OR p.status = :status)
    """,
    )
    fun findPayments(
        @Param("memberId") memberId: Long,
        @Param("status") status: PaymentStatus?,
        pageable: Pageable,
    ): Page<Payment>
}
