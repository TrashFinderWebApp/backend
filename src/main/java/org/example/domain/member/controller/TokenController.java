package org.example.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.domain.member.dto.response.AccessTokenResponse;
import org.example.domain.member.dto.response.ErrorMessage;
import org.example.domain.member.dto.response.TokenInfo;
import org.example.domain.member.service.TokenService;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "auth", description = "토큰 API")
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class TokenController {

    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    @GetMapping("/reissue")
    @Operation(summary = "토큰 재발급", description = "토큰 재발급 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = AccessTokenResponse.class)),
                    headers = @Header(name = "refresh Token", description = "리프레시 토큰, http-only설정, 헤더 속 쿠키로 반환")),
            @ApiResponse(responseCode = "401", description = "1. 헤더에 refresh token이 없을 때\t\n 2. refresh token이 일치하지 않을 때",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> reissueToken(HttpServletRequest request, HttpServletResponse response) {

        String encryptedRefreshToken = jwtProvider.resolveRefreshToken(request);
        if (encryptedRefreshToken == null) {
            return new ResponseEntity<>(new ErrorMessage("헤더에 refresh token이 없습니다. 다시 로그인해주세요."),HttpStatus.UNAUTHORIZED);
        }

        try {
            TokenInfo tokenInfo = tokenService.reIssueToken(encryptedRefreshToken);

            Cookie cookie = new Cookie("refreshToken", tokenInfo.getRefreshToken());
            cookie.setMaxAge(14*24*60*60);//expires in 2 weeks

            cookie.setSecure(true);
            cookie.setHttpOnly(true);

            response.addCookie(cookie);
            String accessToken = tokenInfo.getAccessToken();

            return new ResponseEntity<>(new AccessTokenResponse(accessToken), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()),HttpStatus.UNAUTHORIZED);
        }
    }
}
