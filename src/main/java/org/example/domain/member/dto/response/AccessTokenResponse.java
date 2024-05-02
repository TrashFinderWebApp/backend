package org.example.domain.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class AccessTokenResponse {
    @Schema(description = "로그인 시 주어지는 엑세스 토큰", example = "df234scERFDAEEF2348dsDFfew...")
    private String accessToken;

    @Schema(description = "로그인 시 주어지는 JWT 만료 시간, Unix TimeStamp", example = "180000L")
    private Long jwtExpiredTime;
}
