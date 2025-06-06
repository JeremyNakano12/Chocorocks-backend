package com.puce.chocorocks_backend.models.entities

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "products_stores")
data class ProductStore(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    val store: Store,

    @Column(name = "current_stock")
    var currentStock: Int = 0,

    @Column(name = "min_stock_level")
    val minStockLevel: Int = 0,

    @Column(name = "last_updated")
    val lastUpdated: LocalDateTime = LocalDateTime.now()
) : BaseEntity() {
    init {
        // Constraint único en la base de datos
    }
}