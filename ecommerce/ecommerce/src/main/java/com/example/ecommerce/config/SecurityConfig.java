package com.example.ecommerce.config;

import com.example.ecommerce.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.session.InvalidSessionStrategy;
import org.springframework.security.web.session.SimpleRedirectInvalidSessionStrategy;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/product/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**",
                                "/register",
                                "/login",
                                "/error",
                                "/group-buy/**",           // ← এটা permitAll() করো
                                "/group-buy/join/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .userDetailsService(customUserDetailsService)
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/login?error=true")
                        .loginProcessingUrl("/login")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .maximumSessions(1)
                        .expiredUrl("/login?expired=true")

                )
                .csrf(csrf -> csrf.disable())
                .requestCache(requestCache -> requestCache
                        .requestCache(new HttpSessionRequestCache())
                );

        return http.build();
    }

    // Custom InvalidSessionStrategy — nextUrl হারাবে না
    @Bean
    public InvalidSessionStrategy invalidSessionStrategy() {
        return (request, response) -> {
            String nextUrl = request.getParameter("nextUrl");
            if (nextUrl == null || nextUrl.isEmpty()) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    nextUrl = (String) session.getAttribute("nextUrl");
                }
            }

            String redirectUrl = "/login?invalid=true";
            if (nextUrl != null && !nextUrl.isEmpty()) {
                redirectUrl += "&nextUrl=" + URLEncoder.encode(nextUrl, StandardCharsets.UTF_8);
            }

            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            HttpSession session = request.getSession(false);
            String nextUrl = null;

            // Spring-এর default SavedRequest চেক
            SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
            if (savedRequest != null) {
                nextUrl = savedRequest.getRedirectUrl();
                System.out.println("nextUrl from SavedRequest: " + nextUrl);
            }

            // Custom fallback
            if (nextUrl == null || nextUrl.isEmpty()) {
                nextUrl = (String) session.getAttribute("customNextUrl");
                if (nextUrl != null) {
                    System.out.println("nextUrl from custom String: " + nextUrl);
                }
            }

            if (nextUrl != null && !nextUrl.isEmpty() && nextUrl.startsWith("/")) {
                System.out.println("Redirecting to: " + nextUrl);
                new HttpSessionRequestCache().removeRequest(request, response);
                session.removeAttribute("customNextUrl");
                response.sendRedirect(nextUrl);
            } else {
                System.out.println("No valid nextUrl - Redirecting to home");
                response.sendRedirect("/");
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}