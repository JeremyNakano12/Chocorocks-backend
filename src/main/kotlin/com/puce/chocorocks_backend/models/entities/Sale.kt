package com.puce.chocorocks_backend.models.entities
import java.math.BigDecimal
import jakarta.persistence.*

@Entity
@Table(name = "sales")
data class Sale(

    @Column(name = "sale_number", nullable = false, unique = true, length = 50)
    val saleNumber: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    val client: Client? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    val store: Store,

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", nullable = false, length = 20)
    val saleType: SaleType,

    @Column(name = "subtotal", precision = 10, scale = 2)
    val subtotal: BigDecimal = BigDecimal.ZERO,

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    val discountPercentage: BigDecimal = BigDecimal.ZERO,

    @Column(name = "discount_amount", precision = 10, scale = 2)
    val discountAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "tax_percentage", precision = 5, scale = 2)
    val taxPercentage: BigDecimal = BigDecimal("12.00"),

    @Column(name = "tax_amount", precision = 10, scale = 2)
    val taxAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "total_amount", precision = 10, scale = 2)
    val totalAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "payment_method", length = 50)
    val paymentMethod: String? = null,

    @Column(name = "notes", columnDefinition = "TEXT")
    val notes: String? = null,

    @Column(name = "is_invoiced")
    val isInvoiced: Boolean = false,

    @OneToMany(mappedBy = "sale", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val saleDetails: MutableList<SaleDetail> = mutableListOf(),

    @OneToOne(mappedBy = "sale", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val invoice: Invoice? = null
) : BaseEntity()