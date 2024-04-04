package org.example.domain.member.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
@RequestMapping("/api/auth")
public class TokenController {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    @GetMapping("/reissue")
    public ResponseEntity<?> reissueToken(HttpServletRequest request, HttpServletResponse response) {

        String encryptedRefreshToken = jwtProvider.resolveRefreshToken(request);
        if (encryptedRefreshToken == null) {
            return new ResponseEntity<>("헤더에 refresh token이 없습니다. 다시 로그인해주세요.",HttpStatus.UNAUTHORIZED);
        }

        try {
            TokenInfo tokenInfo = tokenService.reIssueToken(encryptedRefreshToken);

            Cookie cookie = new Cookie("refreshToken", tokenInfo.getRefreshToken());
            cookie.setMaxAge(14*24*60*60);//expires in 2 weeks

            cookie.setSecure(true);
            cookie.setHttpOnly(true);

            response.addCookie(cookie);
            String accessToken = tokenInfo.getAccessToken();

            return new ResponseEntity<>(accessToken, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.UNAUTHORIZED);
        }
    }
}
