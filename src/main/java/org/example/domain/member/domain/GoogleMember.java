package org.example.domain.member.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class GoogleMember {

    @JsonProperty("id")
    private String socialId;

    @JsonProperty("name")
    private String socialName;
}
