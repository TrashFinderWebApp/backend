package org.example.domain.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.domain.member.dto.response.AuthResponseDto.TokenInfo;
import org.example.domain.member.service.TokenService;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
public class TokenController {
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    @GetMapping("/token/reissue")
    public ResponseEntity<?> reissueToken(HttpServletRequest request, String token) {
        String userPk = extractPk(request);
        TokenInfo tokenInfo = tokenService.reIssueToken(, token);

        if (tokenInfo.getAccessToken().isEmpty() || tokenInfo.getRefreshToken().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", tokenInfo.getAccessToken());
        headers.set("RefreshToken", tokenInfo.getRefreshToken());
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    private String extractPk(HttpServletRequest request) {
        return jwtProvider.getAuthentication(request.getHeader("Authorization")
                .substring(7).get)
    }
}
