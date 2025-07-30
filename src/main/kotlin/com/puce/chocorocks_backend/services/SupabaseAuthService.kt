package com.puce.chocorocks_backend.services

import com.puce.chocorocks_backend.dtos.responses.SupabaseUserResponse

interface SupabaseAuthService {
    fun validateToken(token: String): SupabaseUserResponse?
    fun getUserProfile(token: String): SupabaseUserResponse?
}