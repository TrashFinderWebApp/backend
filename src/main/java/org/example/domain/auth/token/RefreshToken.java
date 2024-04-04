package org.example.domain.auth.token;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

@AllArgsConstructor
@Getter
@RedisHash(value = "jwtToken", timeToLive = 60 * 30)//임시로 30분 실제로는 2주할 예정
public class RefreshToken {
    @Id
    private String userPk;


    private String refreshToken;

//60*60*24*3

}
