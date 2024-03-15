package org.example.apis.oauth.domain;

import java.util.Map;

public class GoogleOAuth2Membrer extends OAuth2MemberInfo{

    public GoogleOAuth2Membrer(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getOAuth2Id() {
        return (String) attributes.get("sub");
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
