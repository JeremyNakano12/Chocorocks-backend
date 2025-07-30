package com.puce.chocorocks_backend.security

import com.puce.chocorocks_backend.services.SupabaseAuthService
import com.puce.chocorocks_backend.dtos.responses.SupabaseUserResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtAuthenticationFilter(
    private val supabaseAuthService: SupabaseAuthService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            // Obtener token del header Authorization
            val token = extractTokenFromRequest(request)

            if (token != null && SecurityContextHolder.getContext().authentication == null) {
                // Validar token con Supabase
                val supabaseUser = supabaseAuthService.validateToken(token)

                if (supabaseUser != null) {
                    // Crear autenticación de Spring Security
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_${supabaseUser.role.name}"))

                    val authToken = UsernamePasswordAuthenticationToken(
                        supabaseUser,
                        null,
                        authorities
                    )

                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken

                    logger.debug("Usuario autenticado: ${supabaseUser.email} con rol: ${supabaseUser.role}")
                } else {
                    logger.debug("Token inválido o expirado")
                }
            }
        } catch (e: Exception) {
            logger.error("Error en filtro de autenticación JWT: ${e.message}")
        }

        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")

        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else {
            null
        }
    }
}