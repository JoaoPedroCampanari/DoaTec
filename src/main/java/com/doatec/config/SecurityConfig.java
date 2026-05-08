package com.doatec.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint())
                )
                .authorizeHttpRequests(auth -> auth
                        // 1. RECURSOS ESTÁTICOS E IMAGENS (Sempre Públicos)
                        .requestMatchers("/style.css", "/doatec_logo.png", "/js/**", "/css/**", "/images/**", "/uploads/**").permitAll()

                        // 2. PÁGINAS PÚBLICAS
                        .requestMatchers("/", "/index.html", "/login.html", "/registro.html", "/sobre.html", "/suporte.html").permitAll()

                        // 3. APIs PÚBLICAS
                        .requestMatchers("/api/login", "/api/register", "/api/register/aluno", "/api/register/doador-pf", "/api/register/doador-pj", "/api/dashboard/stats", "/api/suporte").permitAll()

                        // 4. H2 CONSOLE
                        .requestMatchers("/h2-console/**").permitAll()

                        // 5. PROTEÇÃO DE PÁGINAS PRIVADAS (Exige Login)
                        .requestMatchers("/perfil.html", "/aluno.html", "/donate.html", "/minhas-doacoes.html", "/meus-pedidos.html", "/admin.html").authenticated()

                        // 6. PROTEÇÃO DE APIs (Exige Role Específica)
                        .requestMatchers("/api/super-admin/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers("/api/donations/**", "/api/solicitacoes/**", "/api/suporte/**", "/api/users/**", "/api/dashboard/**", "/api/notificacoes/**").hasAnyRole("USER", "ADMIN", "SUPER_ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(ajaxLogoutSuccessHandler())
                        .permitAll()
                );

        return http.build();
    }

    /**
     * AuthenticationEntryPoint personalizado que:
     * - Para páginas HTML: redireciona para login.html
     * - Para APIs: retorna 401 Unauthorized
     */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
            String requestURI = request.getRequestURI();
            String acceptHeader = request.getHeader("Accept");

            // Verifica se é uma requisição de página HTML
            boolean isHtmlRequest = requestURI.endsWith(".html") ||
                    (acceptHeader != null && acceptHeader.contains("text/html"));

            if (isHtmlRequest) {
                // Redireciona para login page
                response.sendRedirect("/login.html");
            } else {
                // Retorna 401 para APIs (JSON)
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Não autorizado\",\"message\":\"Você precisa estar logado para acessar este recurso.\"}");
            }
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public LogoutSuccessHandler ajaxLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            response.setStatus(200);
            response.getWriter().write("{\"message\":\"Logout realizado com sucesso\"}");
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ROLE_SUPER_ADMIN")
                .implies("ROLE_ADMIN")
                .role("ROLE_ADMIN")
                .implies("ROLE_USER")
                .build();
    }
}