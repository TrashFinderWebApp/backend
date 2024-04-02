package org.example.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.member.domain.KakaoMember;
import org.example.domain.member.domain.Member;
import org.example.domain.member.domain.SocialMember;
import org.example.domain.member.dto.response.TokenInfo;
import org.example.domain.member.dto.token.KakaoToken;
import org.example.domain.member.repository.MemberRepository;
import org.example.domain.member.repository.SocialMemberRepository;
import org.example.domain.member.type.RoleType;
import org.example.domain.member.type.SocialType;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class Oauth2Service {
    private final MemberRepository memberRepository;
    private final SocialMemberRepository socialMemberRepository;
    private final JwtProvider jwtProvider;

    @Value("${oauth2.client.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth2.client.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${oauth2.client.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${oauth2.client.token-uri}")
    private String kakaoTokenUri;

    @Value("${oauth2.client.kakao.user-info-uri}")
    private String kakaoUserInfo;

    public TokenInfo socialLogin(SocialType socialType, String code) {
        return switch (socialType) {
            case GOOGLE -> kakaoLogin(code, socialType);
            case NAVER -> naverLogin(code, socialType);
            case KAKAO -> googleLogin(code, socialType);
            default -> throw new IllegalArgumentException("Invalid Provider Type.");
        };
    }

    public TokenInfo kakaoLogin(String code, SocialType socialType) {
        String accessToken = getKakaoAccessToken(code);
        KakaoMember kakaoMember = getKakaoUserInfo(accessToken);
        createSocialUser(kakaoMember.getSocialId(), socialType);
        return jwtProvider.createToken(kakaoMember.getSocialId(), socialType.toString());
    }

    public TokenInfo naverLogin(String code, SocialType socialType) {
        String accessToken = getGoogleAccessToken(code);
        //String socialId = getNaverUserInfo(accessToken);
        return null;
    }

    public TokenInfo googleLogin(String code, SocialType socialType) {
        String accessToken = getNaverAccessToken(code);
        //getGoogleUserInfo(accessToken);

        return null;
    }

    public String getKakaoAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("client_secret", kakaoClientSecret);
        body.add("redirect_uri", kakaoRedirectUri);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers);
        KakaoToken kakaoToken = restTemplate.postForObject(kakaoTokenUri,tokenRequest, KakaoToken.class);

        if (kakaoToken == null) {
            log.error("카카오 토큰 에러");

        }
        System.out.println(kakaoToken.getAccess_token());

        return kakaoToken.getAccess_token();
    }



    public String getNaverAccessToken(String code) {
        return null;
    }

    public String getGoogleAccessToken(String code) {
        return null;
    }

    public KakaoMember getKakaoUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "Bearer " + accessToken);

        //바디 생성
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("property_keys",  "[\"id\"]");

        //헤더 + 바디
        HttpEntity<MultiValueMap<String, String>> memberInfoRequest = new HttpEntity<>(body, headers);
        return restTemplate.postForObject(kakaoUserInfo, memberInfoRequest, KakaoMember.class);
    }

    @Transactional
    public void createSocialUser(String socialId, SocialType socialType) {
        Member member = new Member(RoleType.ROLE_USER);
        socialMemberRepository.save(new SocialMember(socialId, socialType, member));
    }

}
