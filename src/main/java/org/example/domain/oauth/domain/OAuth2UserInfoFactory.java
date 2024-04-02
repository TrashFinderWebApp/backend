package org.example.domain.oauth.domain;

import java.util.Map;
import org.example.domain.member.type.AuthType;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(AuthType authType, Map<String, Object> attributes) {
        return switch (authType) {
            case GOOGLE -> new GoogleOAuth2User(attributes);
            case NAVER -> new NaverOAuth2User(attributes);
            case KAKAO -> new KakaoOAuth2User(attributes);
            default -> throw new IllegalArgumentException("Invalid Provider Type.");
        };
    }
}
