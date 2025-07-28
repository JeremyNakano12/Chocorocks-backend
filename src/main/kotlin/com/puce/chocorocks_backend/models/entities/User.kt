package com.puce.chocorocks_backend.models.entities
import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(

    @Column(name = "name", nullable = false, length = 100)
    val name: String,

    @Column(name = "email", nullable = false, unique = true, length = 150)
    val email: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    val role: UserRole,

    @Enumerated(EnumType.STRING)
    @Column(name = "type_identification", nullable = false, length = 10)
    val typeIdentification: IdentificationType,

    @Column(name = "identification_number", nullable = false, unique = true, length = 20)
    val identificationNumber: String,

    @Column(name = "phone_number", length = 15)
    val phoneNumber: String? = null,

    @Column(name = "is_active")
    val isActive: Boolean = true,

    @OneToMany(mappedBy = "manager")
    val managedStores: MutableList<Store> = mutableListOf(),

    @OneToMany(mappedBy = "user")
    val sales: MutableList<Sale> = mutableListOf(),

    @OneToMany(mappedBy = "user")
    val invoices: MutableList<Receipt> = mutableListOf()
) : BaseEntity()