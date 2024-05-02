package org.example.domain.member.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoMember {

    @JsonProperty("id")
    private String socialId;

    @JsonProperty("properties")
    private KakaoAccount kakaoAccount;

    @Getter
    public static class KakaoAccount {
        @JsonProperty("nickname")
        private String nickName;
    }

}
