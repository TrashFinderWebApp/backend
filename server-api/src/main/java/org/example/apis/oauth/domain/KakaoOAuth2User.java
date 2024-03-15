package org.example.apis.oauth.domain;

import java.util.Map;

public class KakaoOAuth2Member extends OAuth2MemberInfo {

    private final String id;

    public KakaoOAuth2Member(Map<String, Object> attributes) {
        super((Map<String, Object>) attributes.get("kakao_account"));
        this.id = (String) attributes.get("id");
    }

    @Override
    public String getOAuth2Id() {
        return this.id;
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) ((Map<String, Object>) attributes.get("profile")).get("nickname");
    }
}
