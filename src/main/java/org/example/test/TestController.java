package org.example.test;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.domain.member.dto.request.Oauth2Request;
import org.example.domain.member.dto.response.TokenInfo;
import org.example.domain.member.service.Oauth2Service;
import org.example.domain.member.type.SocialType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth2")
public class TestController {

    private final Oauth2Service oauth2Service;
    @RequestMapping("/naver")
    public ResponseEntity<?> naverLogin(@RequestParam String code, HttpServletResponse response) {
        try {
            TokenInfo tokenInfo = oauth2Service.socialLogin(SocialType.NAVER, code);

            Cookie cookie = new Cookie("refreshToken", tokenInfo.getRefreshToken());
            cookie.setMaxAge(14*24*60*60);//expires in 2 weeks
            cookie.setPath("/");

            //cookie.setSecure(true);//http통신에서는 전달x, https에서만 전달
            cookie.setHttpOnly(true);//XSS 예방

            response.addCookie(cookie);
            String accessToken = tokenInfo.getAccessToken();

            return new ResponseEntity<>(accessToken, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestParam String code, HttpServletResponse response) {
        try {
            TokenInfo tokenInfo = oauth2Service.socialLogin(SocialType.GOOGLE, code);

            Cookie cookie = new Cookie("refreshToken", tokenInfo.getRefreshToken());
            cookie.setMaxAge(14*24*60*60);//expires in 2 weeks
            cookie.setPath("/");

            //cookie.setSecure(true);//http통신에서는 전달x, https에서만 전달
            cookie.setHttpOnly(true);//XSS 예방

            response.addCookie(cookie);
            String accessToken = tokenInfo.getAccessToken();

            return new ResponseEntity<>(accessToken, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestParam String code, HttpServletResponse response) {
        try {
            TokenInfo tokenInfo = oauth2Service.socialLogin(SocialType.KAKAO, code);

            Cookie cookie = new Cookie("refreshToken", tokenInfo.getRefreshToken());
            cookie.setMaxAge(14*24*60*60);//expires in 2 weeks
            cookie.setPath("/");

            //cookie.setSecure(true);//http통신에서는 전달x, https에서만 전달
            cookie.setHttpOnly(true);//XSS 예방

            response.addCookie(cookie);
            String accessToken = tokenInfo.getAccessToken();

            return new ResponseEntity<>(accessToken, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(),HttpStatus.BAD_REQUEST);
        }
    }

    @RequestMapping("/test")
    public ResponseEntity<?> errorPrint() {
        String string = null;
        if (string == null) {
            throw new IllegalArgumentException("asdfd");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
