package org.example.domain.member.service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.member.dto.EmailVerificationResult;
import org.example.domain.member.dto.request.UserSignInRequest;
import org.example.domain.member.dto.request.UserSignUpRequest;
import org.example.domain.member.dto.response.TokenInfo;
import org.example.domain.member.repository.MemberRepository;
import org.example.domain.member.domain.Member;
import org.example.domain.member.type.RoleType;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private static final String AUTH_CODE_PREFIX = "AuthCode";

    private final MemberRepository memberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final MailService mailService;
    private final RedisService redisService;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private long authCodeExpirationMillis;

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Transactional
    public void userSignUp(UserSignUpRequest request) {
        memberRepository.save(new Member(request.getEmail(), passwordEncoder.encode(request.getPassword())
                , request.getName(), RoleType.USER));
    }

    @Transactional
    public Member findByUserEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("user doesn't find"));
    }

    @Transactional
    public Member findById(String id) {
        return memberRepository.findById(Long.valueOf(id))
                .orElseThrow(() -> new IllegalArgumentException("userPk doesn't find"));
    }

    public TokenInfo userSignIn(UserSignInRequest request) {
        // 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        // 이때 authentication 는 인증 여부를 확인하는 authenticated 값이 false
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());

        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 매서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        String userPk = findByUserEmail(authentication.getName()).getId().toString();
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(",")); //authentication 객체에서 권한을 반환한다.

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        return jwtProvider.createToken(userPk, authorities);
    }

    public void sendCodeToEmail(String toEmail) {
        String title = "Travel with me 이메일 인증 번호";
        String authCode = this.createCode();
        mailService.sendEmail(toEmail, title, authCode);
        // 이메일 인증 요청 시 인증 번호 Redis에 저장 ( key = "AuthCode " + Email / value = AuthCode )
        redisService.setValues(AUTH_CODE_PREFIX + toEmail,
                authCode, Duration.ofMillis(this.authCodeExpirationMillis));
    }

    private String createCode() {
        int lenth = 6;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < lenth; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            log.debug("MemberService.createCode() exception occur");
            throw new IllegalStateException("No such algorithm available for code generation.");
        }
    }

    public EmailVerificationResult verifiedCode(String email, String authCode) {
        if (redisService.checkExistsValue(AUTH_CODE_PREFIX + email)) {
            boolean authResult = redisService.getValues(AUTH_CODE_PREFIX + email).equals(authCode);
            if(!authResult){
                throw new IllegalArgumentException("not equal");
            }
            return EmailVerificationResult.of(authResult);
        }
        throw new IllegalArgumentException("don't exist auth code.");
    }
}
