package com.puce.chocorocks_backend.config

import com.puce.chocorocks_backend.security.JwtAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.servlet.config.annotation.InterceptorRegistry


@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // Endpoints públicos
                    .requestMatchers("/").permitAll()
                    .requestMatchers("/health").permitAll()
                    .requestMatchers("/api-info").permitAll()

                    // Endpoints de solo lectura - pueden acceder EMPLOYEE y ADMIN
                    .requestMatchers(HttpMethod.GET, "/chocorocks/api/**").hasAnyRole("EMPLOYEE", "ADMIN")

                    // Operaciones de creación y actualización - pueden acceder EMPLOYEE y ADMIN
                    .requestMatchers(HttpMethod.POST, "/chocorocks/api/**").hasAnyRole("EMPLOYEE", "ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/chocorocks/api/**").hasAnyRole("EMPLOYEE", "ADMIN")
                    .requestMatchers(HttpMethod.PATCH, "/chocorocks/api/**").hasAnyRole("EMPLOYEE", "ADMIN")

                    // Reportes básicos - EMPLOYEE y ADMIN pueden ver
                    .requestMatchers(HttpMethod.GET, "/chocorocks/api/reports/sales/**").hasAnyRole("EMPLOYEE", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/chocorocks/api/reports/inventory/**").hasAnyRole("EMPLOYEE", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/chocorocks/api/reports/best-selling-products/**").hasAnyRole("EMPLOYEE", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/chocorocks/api/reports/traceability/**").hasAnyRole("EMPLOYEE", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/chocorocks/api/reports/summary/**").hasAnyRole("EMPLOYEE", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/chocorocks/api/reports/periods").hasAnyRole("EMPLOYEE", "ADMIN")
                    .requestMatchers(HttpMethod.GET, "/chocorocks/api/reports/filters").hasAnyRole("EMPLOYEE", "ADMIN")

                    // Reportes ejecutivos y rentabilidad - solo ADMIN
                    .requestMatchers(HttpMethod.GET, "/chocorocks/api/reports/profitability/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/chocorocks/api/reports/executive-dashboard/**").hasRole("ADMIN")

                    // Operaciones de eliminación - solo ADMIN
                    .requestMatchers(HttpMethod.DELETE, "/chocorocks/api/**").hasRole("ADMIN")

                    // Endpoints críticos - solo ADMIN
                    .requestMatchers("/chocorocks/api/users/**").hasRole("ADMIN")
                    .requestMatchers("/chocorocks/api/stores/**/delete").hasRole("ADMIN")
                    .requestMatchers("/chocorocks/api/categories/**/delete").hasRole("ADMIN")
                    .requestMatchers("/chocorocks/api/user-activities/**").hasRole("ADMIN")

                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()


        configuration.allowedOriginPatterns = listOf(
            "http://localhost:3000",
            "https://*.vercel.app",
        )

        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}