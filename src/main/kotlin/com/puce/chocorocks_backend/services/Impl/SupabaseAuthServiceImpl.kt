package com.puce.chocorocks_backend.services.Impl

import com.puce.chocorocks_backend.dtos.responses.SupabaseUserResponse
import com.puce.chocorocks_backend.dtos.responses.SupabaseCreateUserResponse
import com.puce.chocorocks_backend.models.entities.UserRole
import com.puce.chocorocks_backend.services.SupabaseAuthService
import com.fasterxml.jackson.databind.ObjectMapper
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.SecureRandom
import javax.crypto.SecretKey

@Service
class SupabaseAuthServiceImpl(
    @Value("\${supabase.jwt.secret}") private val jwtSecret: String,
    @Value("\${supabase.url}") private val supabaseUrl: String,
    @Value("\${supabase.anon.key}") private val supabaseAnonKey: String,
    @Value("\${supabase.service.key}") private val supabaseServiceKey: String // NUEVO: Service Key
) : SupabaseAuthService {

    private val logger = LoggerFactory.getLogger(SupabaseAuthServiceImpl::class.java)
    private val objectMapper = ObjectMapper()
    private val httpClient = OkHttpClient()
    private val secureRandom = SecureRandom()

    override fun validateToken(token: String): SupabaseUserResponse? {
        return try {
            val cleanToken = token.removePrefix("Bearer ").trim()

            val claims = validateJwtToken(cleanToken)

            val userId = claims.subject
            val email = claims.get("email", String::class.java)
            val userMetadata = claims.get("user_metadata", Map::class.java) as? Map<String, Any>
            val appMetadata = claims.get("app_metadata", Map::class.java) as? Map<String, Any>

            val roleString = appMetadata?.get("role")?.toString()
                ?: userMetadata?.get("role")?.toString()
                ?: "EMPLOYEE"

            val role = try {
                UserRole.valueOf(roleString.uppercase())
            } catch (e: IllegalArgumentException) {
                logger.warn("Rol inválido para usuario $email: $roleString. Usando EMPLOYEE por defecto.")
                UserRole.EMPLOYEE
            }

            val name = userMetadata?.get("name")?.toString()
                ?: userMetadata?.get("full_name")?.toString()
                ?: email.substringBefore("@")

            SupabaseUserResponse(
                id = userId,
                email = email,
                role = role,
                name = name
            )
        } catch (e: Exception) {
            logger.error("Error validando token de Supabase: ${e.message}")
            null
        }
    }

    override fun getUserProfile(token: String): SupabaseUserResponse? {
        return try {
            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/user")
                .addHeader("Authorization", "Bearer $token")
                .addHeader("apikey", supabaseAnonKey)
                .build()

            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val userProfile = objectMapper.readValue(responseBody, Map::class.java)

                val userId = userProfile["id"]?.toString() ?: return null
                val email = userProfile["email"]?.toString() ?: return null
                val userMetadata = userProfile["user_metadata"] as? Map<String, Any> ?: emptyMap()
                val appMetadata = userProfile["app_metadata"] as? Map<String, Any> ?: emptyMap()

                val roleString = appMetadata["role"]?.toString()
                    ?: userMetadata["role"]?.toString()
                    ?: "EMPLOYEE"

                val role = try {
                    UserRole.valueOf(roleString.uppercase())
                } catch (e: IllegalArgumentException) {
                    UserRole.EMPLOYEE
                }

                val name = userMetadata["name"]?.toString()
                    ?: userMetadata["full_name"]?.toString()
                    ?: email.substringBefore("@")

                SupabaseUserResponse(
                    id = userId,
                    email = email,
                    role = role,
                    name = name
                )
            } else {
                logger.error("Error obteniendo perfil de usuario: ${response.code}")
                null
            }
        } catch (e: Exception) {
            logger.error("Error consultando perfil de Supabase: ${e.message}")
            null
        }
    }

    override fun createUser(email: String, password: String, metadata: Map<String, Any>): SupabaseCreateUserResponse? {
        return try {
            logger.info("🚀 Creando usuario en Supabase: $email")

            val requestBody = mapOf(
                "email" to email,
                "password" to password,
                "user_metadata" to metadata,
                "app_metadata" to mapOf(
                    "role" to metadata["role"],
                    "provider" to "admin_created",
                    "local_user_id" to metadata["user_id"]
                ),
                "email_confirm" to true
            )

            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/admin/users")
                .addHeader("Authorization", "Bearer $supabaseServiceKey")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Content-Type", "application/json")
                .post(objectMapper.writeValueAsString(requestBody).toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                logger.info("✅ Usuario creado exitosamente en Supabase: $email")
                logger.debug("Respuesta Supabase: $responseBody")

                objectMapper.readValue(responseBody, SupabaseCreateUserResponse::class.java)
            } else {
                val errorBody = response.body?.string()
                logger.error("❌ Error creando usuario en Supabase: ${response.code} - $errorBody")
                null
            }
        } catch (e: Exception) {
            logger.error("❌ Excepción creando usuario en Supabase: ${e.message}", e)
            null
        }
    }

    override fun sendPasswordReset(email: String, redirectUrl: String?): Boolean {
        return try {
            logger.info("📧 Enviando reset password para: $email")

            val finalRedirectUrl = redirectUrl ?: "http://23.20.243.209:8080/api-info"

            val requestBody = mapOf(
                "email" to email,
                "redirect_to" to finalRedirectUrl
            )

            val request = Request.Builder()
                .url("$supabaseUrl/auth/v1/recover")
                .addHeader("apikey", supabaseAnonKey)
                .addHeader("Content-Type", "application/json")
                .post(objectMapper.writeValueAsString(requestBody).toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                logger.info("✅ Reset password enviado exitosamente a: $email")
                true
            } else {
                val errorBody = response.body?.string()
                logger.error("❌ Error enviando reset password: ${response.code} - $errorBody")
                false
            }
        } catch (e: Exception) {
            logger.error("❌ Excepción enviando reset password: ${e.message}", e)
            false
        }
    }

    private fun generateSecureTemporaryPassword(): String {
        val upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowerCase = "abcdefghijklmnopqrstuvwxyz"
        val numbers = "0123456789"
        val symbols = "!@#$%^&*"
        val allChars = upperCase + lowerCase + numbers + symbols

        return (1..16).map { allChars[secureRandom.nextInt(allChars.length)] }.joinToString("")
    }

    private fun validateJwtToken(token: String): Claims {
        val secretKey: SecretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}