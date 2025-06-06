package com.puce.chocorocks_backend.models.entities

import jakarta.persistence.*
import java.math.BigDecimal

@Entity
@Table(name = "products")
data class Product(

    @Column(name = "code", nullable = false, unique = true, length = 50)
    val code: String,

    @Column(name = "name_product", nullable = false, length = 150)
    val nameProduct: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    val category: Category,

    @Column(name = "flavor", length = 100)
    val flavor: String? = null,

    @Column(name = "size", length = 50)
    val size: String? = null,

    @Column(name = "production_cost", precision = 10, scale = 2)
    val productionCost: BigDecimal = BigDecimal.ZERO,

    @Column(name = "wholesale_price", precision = 10, scale = 2)
    val wholesalePrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "retail_price", precision = 10, scale = 2)
    val retailPrice: BigDecimal = BigDecimal.ZERO,

    @Column(name = "min_stock_level")
    val minStockLevel: Int = 0,

    @Column(name = "image_url", length = 500)
    val imageUrl: String? = null,

    @Column(name = "barcode", unique = true, length = 50)
    val barcode: String? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true,


    @OneToMany(mappedBy = "product")
    val batches: MutableList<ProductBatch> = mutableListOf(),

    @OneToMany(mappedBy = "product")
    val productStores: MutableList<ProductStore> = mutableListOf(),

    @OneToMany(mappedBy = "product")
    val saleDetails: MutableList<SaleDetail> = mutableListOf()
) : BaseEntity()