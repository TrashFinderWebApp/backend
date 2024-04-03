package org.example.domain.member.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class NaverMember {

    @JsonProperty("resultcode")
    private String resultCode;

    @JsonProperty("message")
    private String message;

    @JsonProperty("response")
    private NaverUserInfo naverUserInfo;

    @Getter
    public static class NaverUserInfo {
        private String id;
        private String name;
        private String email;
    }

}
