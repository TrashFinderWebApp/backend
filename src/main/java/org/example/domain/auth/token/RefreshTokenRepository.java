package org.example.domain.auth.token;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    boolean existsByToken(String accessToken);
    void deleteByToken(String accessToken);
}
