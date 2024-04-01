package org.example.domain.member.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "users", description = "유저 회원가입/로그인 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class Oauth2Controller {

}
