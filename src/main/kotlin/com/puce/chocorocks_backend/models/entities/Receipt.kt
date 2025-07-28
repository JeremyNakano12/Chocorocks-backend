package com.puce.chocorocks_backend.models.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "receipts")
data class Receipt(

    @Column(name = "receipt_number", nullable = false, unique = true, length = 50)
    val receiptNumber: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    val client: Client? = null,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    val sale: Sale,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    val store: Store,

    @CreationTimestamp
    @Column(name = "issue_date")
    val issueDate: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "receipt_status", nullable = false, length = 20)
    val receiptStatus: ReceiptStatus = ReceiptStatus.ACTIVE,

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    val subtotal: BigDecimal,

    @Column(name = "discount_amount", precision = 10, scale = 2)
    val discountAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    val taxPercentage: BigDecimal = BigDecimal("12.00"),

    @Column(name = "tax_amount", precision = 10, scale = 2, nullable = false)
    val taxAmount: BigDecimal,

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    val totalAmount: BigDecimal,

    @Column(name = "payment_method", length = 50)
    val paymentMethod: String? = null,

    @Column(name = "additional_notes", columnDefinition = "TEXT")
    val additionalNotes: String? = null,

    @Column(name = "customer_name", length = 200)
    val customerName: String? = null,

    @Column(name = "customer_identification", length = 20)
    val customerIdentification: String? = null,

    @Column(name = "is_printed")
    val isPrinted: Boolean = false,

    @Column(name = "print_count")
    val printCount: Int = 0

) : BaseEntity()