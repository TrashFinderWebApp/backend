package org.example.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.user.dto.request.UserSignUpRequest;
import org.example.domain.user.repository.UserRepository;
import org.example.domain.user.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)//readOnly 적용 이유 : 성능상 우세
    public User findBySocialId(String socialId) {
        return userRepository.findBySocialId(socialId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void userSignUp(UserSignUpRequest request) {

    }

    @Transactional
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
