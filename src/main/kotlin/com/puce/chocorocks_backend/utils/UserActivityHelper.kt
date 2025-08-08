package com.puce.chocorocks_backend.utils

import com.puce.chocorocks_backend.repositories.UserRepository
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Component
class UserActivityHelper {

    @Autowired
    private lateinit var userRepository: UserRepository

    fun getCurrentUserIpAddress(): String? {
        return try {
            val request = getCurrentHttpRequest()
            request?.let { getClientIpAddress(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserAgent(): String? {
        return try {
            val request = getCurrentHttpRequest()
            request?.getHeader("User-Agent")
        } catch (e: Exception) {
            null
        }
    }


    private fun getCurrentHttpRequest(): HttpServletRequest? {
        val requestAttributes = RequestContextHolder.getRequestAttributes()
        return if (requestAttributes is ServletRequestAttributes) {
            requestAttributes.request
        } else {
            null
        }
    }

    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",")[0].trim()
        }

        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp.trim()
        }

        val xForwarded = request.getHeader("X-Forwarded")
        if (!xForwarded.isNullOrBlank()) {
            return xForwarded.trim()
        }

        val forwardedFor = request.getHeader("Forwarded-For")
        if (!forwardedFor.isNullOrBlank()) {
            return forwardedFor.trim()
        }

        val forwarded = request.getHeader("Forwarded")
        if (!forwarded.isNullOrBlank()) {
            return forwarded.trim()
        }

        return request.remoteAddr ?: "unknown"
    }

    fun getCurrentUserId(): Long {
        return try {
            val authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().authentication

            if (authentication != null && authentication.isAuthenticated) {
                val supabaseUser = authentication.principal as? com.puce.chocorocks_backend.dtos.responses.SupabaseUserResponse

                if (supabaseUser != null) {
                    return findLocalUserIdByEmail(supabaseUser.email) ?: 1L
                }
            }

            1L
        } catch (e: Exception) {
            println("Error obteniendo usuario actual: ${e.message}")
            1L
        }
    }

    private fun findLocalUserIdByEmail(email: String): Long? {
        return try {
            // Buscar usuario por email en la base de datos local
            val user = userRepository.findByEmail(email)
            user?.id
        } catch (e: Exception) {
            println("Error buscando usuario local por email: ${e.message}")
            null
        }
    }

    fun getCurrentUserEmail(): String? {
        return try {
            val authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().authentication
            val supabaseUser = authentication?.principal as? com.puce.chocorocks_backend.dtos.responses.SupabaseUserResponse
            supabaseUser?.email
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentUserRole(): String? {
        return try {
            val authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().authentication
            val supabaseUser = authentication?.principal as? com.puce.chocorocks_backend.dtos.responses.SupabaseUserResponse
            supabaseUser?.role?.name
        } catch (e: Exception) {
            null
        }
    }

    fun createActivityRequest(
        actionType: String,
        tableName: String,
        recordId: Long,
        description: String,
        userId: Long? = null
    ): com.puce.chocorocks_backend.dtos.requests.UserActivityRequest {
        return com.puce.chocorocks_backend.dtos.requests.UserActivityRequest(
            userId = userId ?: getCurrentUserId(),
            actionType = actionType,
            tableAffected = tableName,
            recordId = recordId,
            description = description,
            ipAddress = getCurrentUserIpAddress(),
            userAgent = getCurrentUserAgent()
        )
    }

    fun isUserAuthenticated(): Boolean {
        return try {
            val authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().authentication
            authentication?.isAuthenticated == true && authentication.principal is com.puce.chocorocks_backend.dtos.responses.SupabaseUserResponse
        } catch (e: Exception) {
            false
        }
    }

    fun getCurrentUserInfo(): Map<String, Any?> {
        return try {
            val authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().authentication
            val supabaseUser = authentication?.principal as? com.puce.chocorocks_backend.dtos.responses.SupabaseUserResponse

            if (supabaseUser != null) {
                mapOf(
                    "supabaseId" to supabaseUser.id,
                    "email" to supabaseUser.email,
                    "name" to supabaseUser.name,
                    "role" to supabaseUser.role.name,
                    "localUserId" to getCurrentUserId(),
                    "ipAddress" to getCurrentUserIpAddress(),
                    "userAgent" to getCurrentUserAgent()
                )
            } else {
                emptyMap()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}