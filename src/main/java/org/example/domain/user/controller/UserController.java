package org.example.domain.user.controller;

import java.net.http.HttpResponse;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.domain.user.dto.request.UserSignUpRequest;
import org.example.domain.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> userSingUp(@Valid @RequestBody UserSignUpRequest request) {
        if (isDuplicated(request.getEmail())) {
            return new ResponseEntity<>("이메일 중복입니다. 다시 입력해주세요.", HttpStatus.BAD_REQUEST);
        }

        userService.userSignUp(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private boolean isDuplicated(String email) {
        return userService.existsByEmail(email);
    }
}
