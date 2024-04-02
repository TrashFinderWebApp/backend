package org.example.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.domain.member.dto.request.UserSignInRequest;
import org.example.domain.member.dto.request.UserSignUpRequest;
import org.example.domain.member.dto.response.TokenInfo;
import org.example.domain.member.service.MemberService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "members", description = "유저 회원가입/로그인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/signup")
    @Operation(summary = "유저 회원가입", description = "서비스 내 회원가입 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "1. 이메일 중복 \t\n 2. 비밀번호 불일치 \t\n "
                    + "3. 이메일 혹은 비밀번호 형식이 맞지 않습니다. \t\n 4. 이메일, 비밀번호, 이름이 비어 있습니다.")
    })
    public ResponseEntity<?> userSignUp(@Valid @RequestBody UserSignUpRequest request) {
        if (isDuplicated(request.getEmail())) {
            return new ResponseEntity<>("이메일 중복입니다. 다시 입력해주세요.", HttpStatus.BAD_REQUEST);
        }

        memberService.userSignUp(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/signin")
    @Operation(summary = "유저 로그인", description = "서비스 내 로그인 API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "500", description = "서버에러")
    })
    public ResponseEntity<?> userSignIn(@Valid @RequestBody UserSignInRequest request) {
        TokenInfo tokenInfo = memberService.userSignIn(request);

        if (tokenInfo.getAccessToken().isEmpty() || tokenInfo.getRefreshToken().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", tokenInfo.getAccessToken());
        headers.set("RefreshToken", tokenInfo.getRefreshToken());
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    private boolean isDuplicated(String email) {
        return memberService.existsByEmail(email);
    }

}
