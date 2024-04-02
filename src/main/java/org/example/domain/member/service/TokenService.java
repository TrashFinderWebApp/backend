package org.example.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.auth.token.RefreshToken;
import org.example.domain.auth.token.RefreshTokenRepository;
import org.example.domain.member.dto.response.TokenInfo;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.security.core.Authentication;

@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;

    public TokenInfo reIssueToken(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findById(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("DB에 리프레시 토큰이 없습니다."));

        refreshTokenRepository.deleteByToken(refreshToken);
        Authentication authentication = jwtProvider.getAuthentication(token.getAccessToken());

        return jwtProvider.createToken(authentication);
    }
}
