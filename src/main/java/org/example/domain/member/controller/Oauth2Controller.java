package org.example.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.domain.member.dto.request.Oauth2Request;
import org.example.domain.member.dto.response.AccessTokenResponse;
import org.example.domain.member.dto.response.ErrorMessage;
import org.example.domain.member.dto.response.TokenInfo;
import org.example.domain.member.service.Oauth2Service;
import org.example.domain.member.type.SocialType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "oauth2", description = "유저 소셜 로그인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth2")
public class Oauth2Controller {

    private final Oauth2Service oauth2Service;


    @PostMapping("/login")
    @Operation(summary = "유저 소셜 로그인", description = "소셜 로그인 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "소셜 타입 혹은 코드가 없을 때"),
            @ApiResponse(responseCode = "500", description = "서버 인증 에러")
    })
    public ResponseEntity<?> socialLogin(@RequestBody Oauth2Request oauth2Request, HttpServletResponse response) {
        SocialType socialType = oauth2Request.getSocialType();
        String socialAccessToken = oauth2Request.getSocialAccessToken();
        if (socialType == null || socialAccessToken == null) {
            return new ResponseEntity<>(new ErrorMessage("소셜 타입 혹은 코드가 없습니다."),HttpStatus.BAD_REQUEST);
        }

        try {
            TokenInfo tokenInfo = oauth2Service.socialLogin(socialType, socialAccessToken);


            Cookie cookie = new Cookie("refreshToken", tokenInfo.getRefreshToken());
            cookie.setMaxAge(14*24*60*60);//expires in 2 weeks
            cookie.setPath("/");

            //cookie.setSecure(true);//http통신에서는 전달x, https에서만 전달
            cookie.setHttpOnly(true);//XSS 예방

            response.addCookie(cookie);
            String accessToken = tokenInfo.getAccessToken();

            return new ResponseEntity<>(new AccessTokenResponse(accessToken), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()),HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
