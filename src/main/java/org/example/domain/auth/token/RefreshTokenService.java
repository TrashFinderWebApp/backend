package org.example.domain.auth.token;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void saveTokenInfo(String accessToken, String refreshToken) {
        refreshTokenRepository.save(new RefreshToken(accessToken, refreshToken));
    }
}
