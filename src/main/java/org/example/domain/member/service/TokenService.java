package org.example.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.auth.token.RefreshTokenRepository;
import org.example.domain.member.dto.response.AuthResponseDto.TokenInfo;
import org.example.global.security.jwt.JwtProvider;

@RequiredArgsConstructor
public class TokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final
    public TokenInfo reIssueToken(String userPk, String token) {
        if (refreshTokenRepository.existsByToken(token)) {
            refreshTokenRepository.deleteByToken(token);
        }
        jwtProvider.generateToken()
    }
}
