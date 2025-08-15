package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.responses.SupabaseUserResponse
import com.puce.chocorocks_backend.dtos.responses.SupabaseCreateUserResponse

interface SupabaseAuthService {

    fun validateToken(token: String): SupabaseUserResponse?

    fun getUserProfile(token: String): SupabaseUserResponse?

    fun createUser(email: String, password: String, metadata: Map<String, Any>): SupabaseCreateUserResponse?

    fun sendPasswordReset(email: String, redirectUrl: String? = null): Boolean
}