package org.example.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.example.domain.member.dto.EmailVerificationResult;
import org.example.domain.member.dto.request.UserSignInRequest;
import org.example.domain.member.dto.request.UserSignUpRequest;
import org.example.domain.member.dto.response.AccessTokenResponse;
import org.example.domain.member.dto.response.ErrorMessage;
import org.example.domain.member.dto.response.TokenInfo;
import org.example.domain.member.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "members", description = "유저 회원가입/로그인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/signup")
    @Operation(summary = "유저 회원가입", description = "서비스 내 회원가입 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = AccessTokenResponse.class)),
                    headers = @Header(name = "refreshToken", description = "리프레시 토큰, http-only설정, 헤더 속 쿠키로 반환")),
            @ApiResponse(responseCode = "400", description = "1. 이메일 중복 \t\n 2. 비밀번호 불일치 \t\n "
                    + "3. 이메일 혹은 비밀번호 형식이 맞지 않습니다. \t\n 4. 이메일, 비밀번호, 이름이 비어 있습니다. \t\n"
                    + "5. 인증 코드가 잘못 되었습니다.",
            content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> userSignUp(@Valid @RequestBody UserSignUpRequest request) {
        try{
            EmailVerificationResult verificationResult = memberService.verifiedCode(request.getEmail(), request.getAuthCode());
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        if (isNotMatchedPassword(request)) {
            return new ResponseEntity<>(new ErrorMessage("비밀번호가 일치하지 않습니다. 다시 입력해주세요."), HttpStatus.BAD_REQUEST);
        }
        if (isDuplicated(request.getEmail())) {
            return new ResponseEntity<>(new ErrorMessage("이메일 중복입니다. 다시 입력해주세요."), HttpStatus.BAD_REQUEST);
        }

        memberService.userSignUp(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/emails/verification-requests")
    @Operation(summary = "이메일 인증 코드 전송", description = "유저가 입력한 이메일로 인증 코드를 전송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 코드 전송 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 이메일 형식", content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류로 인증 코드 전송 실패", content = @Content(schema = @Schema(implementation = ErrorMessage.class)))
    })
    public ResponseEntity<?> sendMessage(@RequestParam("email") @Valid @Email String email) {
        try {
            memberService.sendCodeToEmail(email);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorMessage("잘못된 이메일 형식입니다."));
        } catch (Exception e) {
            return new ResponseEntity<>(new ErrorMessage("인증 코드 전송에 실패했습니다."), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/emails/verifications")
    @Operation(summary = "이메일 인증 코드 확인", description = "유저가 입력한 이메일과 인증 코드의 유효성을 검증합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증 성공", content = @Content(schema = @Schema(implementation = EmailVerificationResult.class))),
            @ApiResponse(responseCode = "400", description = "인증 실패", content = @Content(schema = @Schema(implementation = EmailVerificationResult.class)))
    })
    public ResponseEntity<?> verificationEmail(@RequestParam("email") @Valid @Email String email,
            @RequestParam("code") String authCode) {

        try{
            EmailVerificationResult response = memberService.verifiedCode(email, authCode);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getMessage()));
        }
    }

    @PostMapping("/signin")
    @Operation(summary = "유저 로그인", description = "서비스 내 로그인 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = AccessTokenResponse.class)),
                    headers = @Header(name = "refreshToken", description = "리프레시 토큰, http-only설정, 헤더 속 쿠키로 반환")),
            @ApiResponse(responseCode = "401", description = "1. 비밀번호가 일치하지 않을 때\t\n 2. 아이디가 존재하지 않을 때",
                    content = @Content(schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "500", description = "서버에러")
    })
    public ResponseEntity<?> userSignIn(@RequestBody @Valid UserSignInRequest request, HttpServletResponse response) {
        try {
            TokenInfo tokenInfo = memberService.userSignIn(request);

            Cookie cookie = new Cookie("refreshToken", tokenInfo.getRefreshToken());
            cookie.setMaxAge(14 * 24 * 60 * 60);//expires in 2 weeks

            cookie.setSecure(true);
            cookie.setHttpOnly(true);

            response.addCookie(cookie);
            String accessToken = tokenInfo.getAccessToken();

            return new ResponseEntity<>(new AccessTokenResponse(accessToken), HttpStatus.OK);
        } catch (BadCredentialsException e) {
            return new ResponseEntity<>(new ErrorMessage("아이디나 비밀번호가 일치하지 않습니다."), HttpStatus.UNAUTHORIZED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ErrorMessage("사용자 아이디가 존재하지 않습니다."),HttpStatus.UNAUTHORIZED);
        }
    }

    private boolean isDuplicated(String email) {
        return memberService.existsByEmail(email);
    }

    private boolean isNotMatchedPassword(UserSignUpRequest request) {
        return !request.getPassword().equals(request.getMatchPassword());
    }
}
