package org.example.domain.member.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.domain.member.dto.response.TokenInfo;
import org.example.domain.member.service.TokenService;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@RequestMapping("/auth")
public class TokenController {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    @GetMapping("/reissue")
    public ResponseEntity<?> reissueToken(HttpServletRequest request) {

        String encryptedRefreshToken = jwtProvider.resolveRefreshToken(request);
        if (encryptedRefreshToken == null) {
            return new ResponseEntity<>("헤더에 refresh token이 없습니다. 다시 로그인해주세요.",HttpStatus.UNAUTHORIZED);
        }

        try {
            TokenInfo tokenInfo = tokenService.reIssueToken(encryptedRefreshToken);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", tokenInfo.getAccessToken());
            headers.set("RefreshToken", tokenInfo.getRefreshToken());

            return new ResponseEntity<>(headers, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
        }
    }
}
