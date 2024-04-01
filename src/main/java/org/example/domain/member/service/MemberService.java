package org.example.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.member.dto.request.UserSignInRequest;
import org.example.domain.member.dto.request.UserSignUpRequest;
import org.example.domain.member.dto.response.AuthResponseDto;
import org.example.domain.member.dto.response.AuthResponseDto.TokenInfo;
import org.example.domain.member.repository.MemberRepository;
import org.example.domain.member.domain.Member;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional(readOnly = true)//readOnly 적용 이유 : 성능상 우세
    public Member findBySocialId(String socialId) {
        return memberRepository.findBySocialId(socialId)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    @Transactional
    public void userSignUp(UserSignUpRequest request) {
        memberRepository.save(new Member(request.getEmail(), request.getPassword()
                , request.getName()));
    }

    @Transactional
    public Member findByUserEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("user doesn't find"));
    }

    public AuthResponseDto.TokenInfo userSignIn(UserSignInRequest request) {
        Member member = findByUserEmail(request.getEmail());
        if (passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            //return jwtProvider.generateToken(member.getId().toString(), member.getRole());
        }
        return new TokenInfo("", "");
    }
}
