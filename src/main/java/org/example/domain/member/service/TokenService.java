package org.example.domain.member.service;

import io.jsonwebtoken.Claims;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.auth.token.RefreshToken;
import org.example.domain.auth.token.RefreshTokenRepository;
import org.example.domain.auth.token.RefreshTokenService;
import org.example.domain.member.domain.Member;
import org.example.domain.member.dto.response.TokenInfo;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class TokenService {
    private final RefreshTokenService refreshTokenService;
    private final MemberService memberService;
    private final JwtProvider jwtProvider;

    public TokenInfo reIssueToken(String refreshToken) {
        Claims claims = jwtProvider.parseClaims(refreshToken);
        Member member = memberService.findById(claims.getSubject());
        String userPk = member.getId().toString();

        RefreshToken findRefreshToken = refreshTokenService.findById(userPk);

        if (refreshToken.equals(findRefreshToken.getRefreshToken())) {
            return jwtProvider.createToken(userPk, member.getRole().toString());
        }

        refreshTokenService.deleteById(userPk);
        log.error("리프레시 토큰이 일치하지 않아요.");
        throw new IllegalArgumentException("리프레시 토큰이 일치하지 않아요.");
    }
}
