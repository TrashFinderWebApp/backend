package org.example.domain.member.dto.request;


import lombok.Getter;
import org.example.domain.member.type.SocialType;

@Getter
public class Oauth2Request {
    private SocialType socialType;
    private String code;

}
