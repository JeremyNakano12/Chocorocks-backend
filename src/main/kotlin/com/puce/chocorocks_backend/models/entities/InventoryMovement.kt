package com.puce.chocorocks_backend.models.entities

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "inventory_movements")
data class InventoryMovement(

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    val movementType: MovementType,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    val product: Product,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    val batch: ProductBatch? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_store_id")
    val fromStore: Store? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_store_id")
    val toStore: Store? = null,

    @Column(name = "quantity", nullable = false)
    val quantity: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 100)
    val reason: MovementReason,

    @Column(name = "reference_id")
    val referenceId: Long? = null,

    @Column(name = "reference_type", length = 20)
    val referenceType: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "notes", columnDefinition = "TEXT")
    val notes: String? = null,

    @CreationTimestamp
    @Column(name = "movement_date")
    val movementDate: LocalDateTime = LocalDateTime.now()
) : BaseEntity()