package com.puce.chocorocks_backend.models.entities

import jakarta.persistence.*

@Entity
@Table(name = "clients")
data class Client(

    @Column(name = "name_lastname", nullable = false, length = 150)
    val nameLastname: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type_identification", nullable = false, length = 10)
    val typeIdentification: IdentificationType,

    @Column(name = "identification_number", nullable = false, unique = true, length = 20)
    val identificationNumber: String,

    @Column(name = "phone_number", length = 15)
    val phoneNumber: String? = null,

    @Column(name = "email", length = 150)
    val email: String? = null,

    @Column(name = "address", columnDefinition = "TEXT")
    val address: String? = null,

    @Column(name = "requires_invoice")
    val requiresInvoice: Boolean = false,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @OneToMany(mappedBy = "client", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val sales: MutableList<Sale> = mutableListOf(),

    @OneToMany(mappedBy = "client", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val invoices: MutableList<Receipt> = mutableListOf()
) : BaseEntity()