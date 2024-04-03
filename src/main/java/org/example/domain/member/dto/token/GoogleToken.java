package org.example.domain.member.dto.token;

import lombok.Data;

@Data
public class GoogleToken {
    private String token_type;
    private String access_token;
    private String id_token;
    private int expires_in;
    private String scope;
}
