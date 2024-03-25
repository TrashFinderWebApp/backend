package org.example.domain.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class OAuth2ResponseDto {

    @Builder
    @Getter
    @AllArgsConstructor
    public static class TokenInfo {
        private String accessToken;
        private String refreshToken;
    }

}
