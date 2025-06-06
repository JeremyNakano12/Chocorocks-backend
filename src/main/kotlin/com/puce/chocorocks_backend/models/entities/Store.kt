package com.puce.chocorocks_backend.models.entities

import jakarta.persistence.*
import java.time.LocalTime

@Entity
@Table(name = "stores")
data class Store(

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    val address: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    val manager: User? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "type_store", nullable = false, length = 20)
    val typeStore: StoreType,

    @Column(name = "phone_number", length = 15)
    val phoneNumber: String? = null,

    @Column(name = "schedule_open")
    val scheduleOpen: LocalTime? = null,

    @Column(name = "schedule_closed")
    val scheduleClosed: LocalTime? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @OneToMany(mappedBy = "store", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val sales: MutableList<Sale> = mutableListOf(),

    @OneToMany(mappedBy = "store", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val productStores: MutableList<ProductStore> = mutableListOf()
) : BaseEntity()