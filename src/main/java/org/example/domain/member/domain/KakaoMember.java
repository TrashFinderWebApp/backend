package org.example.domain.member.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class KakaoMember {

    @JsonProperty("id")
    private String socialId;

}
