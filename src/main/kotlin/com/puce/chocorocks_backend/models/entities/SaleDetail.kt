package com.puce.chocorocks_backend.models.entities

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "sale_details")
data class SaleDetail(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    val sale: Sale,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    val batch: ProductBatch? = null,

    @Column(name = "quantity", nullable = false)
    val quantity: Int,

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    val unitPrice: BigDecimal,

    @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
    val subtotal: BigDecimal,

    ) : BaseEntity()
