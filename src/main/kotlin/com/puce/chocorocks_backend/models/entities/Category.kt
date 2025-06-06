package com.puce.chocorocks_backend.models.entities

import jakarta.persistence.*


@Entity
@Table(name = "categories")
data class Category(

    @Column(name = "name", nullable = false, unique = true, length = 100)
    val name: String,

    @Column(name = "description", columnDefinition = "TEXT")
    val description: String? = null,

    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val products: MutableList<Product> = mutableListOf()
) : BaseEntity()