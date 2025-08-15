package com.puce.chocorocks_backend.dtos.responses

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SupabaseCreateUserResponse(
    @JsonProperty("id")
    val id: String,

    @JsonProperty("email")
    val email: String,

    @JsonProperty("created_at")
    val createdAt: String,

    @JsonProperty("email_confirmed_at")
    val emailConfirmedAt: String? = null,

    @JsonProperty("user_metadata")
    val userMetadata: Map<String, Any>? = null,

    @JsonProperty("app_metadata")
    val appMetadata: Map<String, Any>? = null,

    @JsonProperty("role")
    val role: String? = null,

    @JsonProperty("aud")
    val aud: String? = null,

    @JsonProperty("phone")
    val phone: String? = null,

    @JsonProperty("phone_confirmed_at")
    val phoneConfirmedAt: String? = null,

    @JsonProperty("confirmation_sent_at")
    val confirmationSentAt: String? = null,

    @JsonProperty("recovery_sent_at")
    val recoverySentAt: String? = null,

    @JsonProperty("email_change")
    val emailChange: String? = null,

    @JsonProperty("email_change_sent_at")
    val emailChangeSentAt: String? = null,

    @JsonProperty("email_change_confirm_status")
    val emailChangeConfirmStatus: Int? = null,

    @JsonProperty("banned_until")
    val bannedUntil: String? = null,

    @JsonProperty("invited_at")
    val invitedAt: String? = null,

    @JsonProperty("updated_at")
    val updatedAt: String? = null,

    @JsonProperty("last_sign_in_at")
    val lastSignInAt: String? = null
)