package org.example.domain.member.dto.request;


import javax.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class Oauth2Request {

    @NotBlank(message = "socialType이 없습니다.")
    private String socialType;

    @NotBlank(message = "socialAccessToken이 없습니다.")
    private String socialAccessToken;

}
