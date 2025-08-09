package rw.health.ubuzima.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import rw.health.ubuzima.security.JwtAuthFilter;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints that don't require authentication
                .requestMatchers("/error/**").permitAll() // Allow access to error pages
                .requestMatchers("/api/v1/health/**", "/health/**").permitAll()
                .requestMatchers("/api/v1/auth/**", "/auth/**").permitAll()
                .requestMatchers("/actuator/health").permitAll() // Only expose health endpoint
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/api/v1/facilities/**", "/facilities/**").permitAll()
                
                // Protected endpoints require authentication
                .requestMatchers("/api/v1/health-worker/**", "/health-worker/**").hasRole("HEALTH_WORKER")
                .requestMatchers("/api/v1/messages/**", "/messages/**").authenticated()
                .requestMatchers("/api/v1/users/**", "/users/**").authenticated()
                .requestMatchers("/api/v1/community-events/**", "/community-events/**").authenticated()
                
                // Actuator endpoints (except health) require admin role
                .requestMatchers("/actuator/**").hasRole("ADMIN")

                // Admin-only endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // Client endpoints (accessible by all authenticated users)
                .requestMatchers("/client/**").hasAnyRole("ADMIN", "HEALTH_WORKER", "CLIENT")

                // Other endpoints require authentication
                .anyRequest().authenticated()
            )
            // Add JWT filter before the standard authentication filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
