package com.puce.chocorocks_backend.models.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "invoices")
data class Invoice(

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    val invoiceNumber: String,

    @Column(name = "invoice_serial", nullable = false, length = 20)
    val invoiceSerial: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    val client: Client,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    val sale: Sale,

    @CreationTimestamp
    @Column(name = "date_emission")
    val dateEmission: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(name = "invoice_status", nullable = false, length = 20)
    val invoiceStatus: InvoiceStatus = InvoiceStatus.PENDING,

    @Column(name = "social_reason", length = 200)
    val socialReason: String? = null,

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    val subtotal: BigDecimal,

    @Column(name = "discount_amount", precision = 10, scale = 2)
    val discountAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "tax_type", length = 20)
    val taxType: String = "IVA",

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    val taxPercentage: BigDecimal = BigDecimal("12.00"),

    @Column(name = "tax_amount", precision = 10, scale = 2, nullable = false)
    val taxAmount: BigDecimal,

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    val totalAmount: BigDecimal,

    @Column(name = "payment_method", length = 50)
    val paymentMethod: String? = null,

    @Column(name = "additional_details", columnDefinition = "TEXT")
    val additionalDetails: String? = null,

    @Column(name = "email_sent")
    val emailSent: Boolean = false,

    @Column(name = "sri_authorization", length = 100)
    val sriAuthorization: String? = null,

    @Column(name = "xml_file_path", length = 500)
    val xmlFilePath: String? = null,

    @Column(name = "pdf_file_path", length = 500)
    val pdfFilePath: String? = null,

    ) : BaseEntity()