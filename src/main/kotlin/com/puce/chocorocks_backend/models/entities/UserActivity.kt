package com.puce.chocorocks_backend.models.entities

import jakarta.persistence.*

@Entity
@Table(name = "user_activities")
data class UserActivity(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "action_type", nullable = false, length = 50)
    val actionType: String,

    @Column(name = "table_affected", length = 50)
    val tableAffected: String? = null,

    @Column(name = "record_id")
    val recordId: Long? = null,

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    val description: String,

    @Column(name = "ip_address")
    val ipAddress: String? = null,

    @Column(name = "user_agent", columnDefinition = "TEXT")
    val userAgent: String? = null,

) : BaseEntity()