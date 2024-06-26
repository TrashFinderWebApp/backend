package org.example.global.security;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.domain.member.type.RoleType;
import org.example.global.advice.ErrorMessage;
import org.example.global.security.filter.JwtAuthenticationFilter;
import org.example.global.security.handler.CustomAccessDeniedHandler;
import org.example.global.security.handler.CustomAuthenticationEntryPoint;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        /* cors, http basic, csrf, 기본 login, session 설정 off */
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(HttpBasicConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        /* 권한에 대한 접근 */
        http.authorizeHttpRequests(authorization -> {
            authorization
                    .requestMatchers("/api/trashcan/registrations/**", "/api/trashcan/suggestions/**")
                    .hasAnyRole(RoleType.USER.name(), RoleType.ADMIN.name())
                    .requestMatchers("/api/notification/list/**").permitAll()
                    .requestMatchers("/admin/**", "/api/notification/", "/api/notification/{id}")
                    .hasAnyRole(RoleType.ADMIN.name())
                    .anyRequest().permitAll();
        });

        http
                .exceptionHandling(e -> {
                    e.accessDeniedHandler(customAccessDeniedHandler);
                    e.authenticationEntryPoint(customAuthenticationEntryPoint);
                });

        http
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/", "/oauth2**",
                "/swagger-ui/**", "/api-docs/**"); //swagger
    }

    //cors 설정
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "https://tfinder.store", "http://localhost:3000", "http://localhost:8080/swagger-ui", "https://tfinder.vercel.app/"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "PUT", "PATCH", "DELETE"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /* 스프링 시큐리티에서 지원하는 패스워드 암호화 방식*/
    /* 기본적으로 BCrypt 방식을 지원한다. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
