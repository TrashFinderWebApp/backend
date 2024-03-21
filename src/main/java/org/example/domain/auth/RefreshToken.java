package org.example.domain.auth;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@AllArgsConstructor
@Getter
@RedisHash(value = "jwtToken", timeToLive = 60*60*24*3)
public class RefreshToken {
    @Id
    private String id;

    private String refreshToken;

    @Indexed
    private String accessToken;
}