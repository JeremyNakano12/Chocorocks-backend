package com.puce.chocorocks_backend.models.entities

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "product_batches")
data class ProductBatch(

    @Column(name = "batch_code", nullable = false, unique = true, length = 50)
    val batchCode: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @Column(name = "production_date", nullable = false)
    val productionDate: LocalDate,

    @Column(name = "expiration_date", nullable = false)
    val expirationDate: LocalDate,

    @Column(name = "initial_quantity", nullable = false)
    val initialQuantity: Int,

    @Column(name = "current_quantity", nullable = false)
    var currentQuantity: Int,

    @Column(name = "batch_cost", precision = 10, scale = 2, nullable = false)
    val batchCost: BigDecimal,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    val store: Store? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @OneToMany(mappedBy = "batch", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val saleDetails: MutableList<SaleDetail> = mutableListOf()
) : BaseEntity()