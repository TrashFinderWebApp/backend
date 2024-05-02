package org.example.domain.auth.token;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void saveTokenInfo(String userPk, String refreshToken) {
        refreshTokenRepository.save(new RefreshToken(userPk, refreshToken));
    }
    @Transactional(readOnly = true)
    public RefreshToken findById(String userPk) {
        return refreshTokenRepository.findById(userPk)
                .orElseThrow(() -> new IllegalArgumentException("db에 리프레시 토큰이 없습니다."));
    }

    @Transactional
    public void deleteById(String userPk) {
        refreshTokenRepository.deleteById(userPk);
    }
}
