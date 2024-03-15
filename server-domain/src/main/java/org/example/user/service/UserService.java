package org.example.user.service;

import lombok.RequiredArgsConstructor;
import org.example.user.domain.User;
import org.example.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)//readOnly 적용 이유 : 성능상 우세
    public User findBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId)
                .orElseThrow(() -> new IllegalArgumentException("not found socialId"));
    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
