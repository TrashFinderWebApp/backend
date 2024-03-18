package org.example.global.security;


import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.example.domain.oauth.service.CustomOAuth2UserService;
import org.example.global.cookie.CookieAuthorizationRequestRepository;
import org.example.global.security.filter.JwtAuthenticationFilter;
import org.example.global.security.jwt.JwtProvider;
import org.example.global.security.oauth.handler.OAuth2AuthenticationFailureHandler;
import org.example.global.security.oauth.handler.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@RequiredArgsConstructor
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final JwtProvider jwtProvider;
    private final CookieAuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;


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
            /*authorization
                    .requestMatchers("/", "/login", "/signup").permitAll()
                    .requestMatchers("/admin").hasAuthority(ADMIN.getValue())
                    .requestMatchers("/user").has*/

            /* 우선적으로 모든 인증 요청에 대해 권한 허용, 이후 인증자만 접근 가능. 추후 세부 수정 예정 */
            authorization
                    .requestMatchers("/oauth2/**").permitAll()
                    .anyRequest().authenticated();
        });

        //oauth2Login
        http
                .oauth2Login(
                        oauth -> {
                            //FE에서 http://localhost:8080/oauth2/authorize/{provider}?redirect_uri=<redirect_uri_after_login> 요청으로 보내며 시작.
                            //이때 Spring Security의 OAuth2 클라이언트는 user를 provider가 제공하는 AuthorizationUrl로 redirect 한다.
                            //Authorization request와 관련된 state는 authorizationRequestRepository 에 저장된다
                            oauth.authorizationEndpoint(author -> {
                                author.baseUri("/oauth2/authorize");
                                author.authorizationRequestRepository(cookieAuthorizationRequestRepository);// 인증 요청을 cookie 에 저장
                            });

                            //유저가 앱에 대한 권한을 모두 허용 시 provider는 사용자를 callback url로 redirect한다. 이때 사용자 인증코드 (authroization code) 도 함께 갖고있다.
                            //거부 시 callbackUrl로 redirect, error 발생
                            oauth.redirectionEndpoint(
                                    redirect -> redirect.baseUri("/oauth2/callback/*")); // 소셜 인증 후 redirect url

                            //콜백 성공 & 사용자 인증코드 포함 시, access_token 에 대한 authroization code를 교환하고, customOAuth2UserService 호출
                            oauth.userInfoEndpoint(userService -> userService.userService(
                                    customOAuth2UserService));//userService()는 OAuth2 인증 과정에서 Authentication 생성에 필요한 OAuth2User 를 반환하는 클래스를 지정한다.// 회원 정보 처리

                            //콜백 결과 성공 시 성공 핸들러 호출
                            oauth.successHandler(oAuth2AuthenticationSuccessHandler);

                            //콜백 결과 실패 시 실패 핸들러 호출
                            oauth.failureHandler(oAuth2AuthenticationFailureHandler);
                        }
                );

        http
                .logout(
                        out -> {
                            out.clearAuthentication(true);
                            out.deleteCookies("JSESSIONID");
                        }
                );

        http
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    //cors 설정
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://프론트 주소 적기");
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "OPTIONS", "PUT", "PATCH", "DELETE"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
