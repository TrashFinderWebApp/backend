package org.example.apis.oauth.domain;

import java.util.Map;

public class NaverOAuth2Member extends OAuth2MemberInfo{
    public NaverOAuth2Member(Map<String, Object> attributes) {
        super((Map<String, Object>) attributes.get("response"));
    }

    @Override
    public String getOAuth2Id() {
        return (String) attributes.get("id");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

}
