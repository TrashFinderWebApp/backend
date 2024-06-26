package org.example.domain.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.member.domain.GoogleMember;
import org.example.domain.member.domain.KakaoMember;
import org.example.domain.member.domain.Member;
import org.example.domain.member.domain.NaverMember;
import org.example.domain.member.domain.SocialMember;
import org.example.domain.member.dto.response.TokenInfo;
import org.example.domain.member.dto.token.GoogleToken;
import org.example.domain.member.dto.token.KakaoToken;
import org.example.domain.member.dto.token.NaverToken;
import org.example.domain.member.repository.MemberRepository;
import org.example.domain.member.repository.SocialMemberRepository;
import org.example.domain.member.type.RoleType;
import org.example.domain.member.type.SocialType;
import org.example.global.security.jwt.JwtProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class Oauth2Service {
    private final SocialMemberRepository socialMemberRepository;
    private final JwtProvider jwtProvider;

    @Value("${oauth2.client.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth2.client.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${oauth2.client.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    @Value("${oauth2.client.kakao.token-uri}")
    private String kakaoTokenUri;

    @Value("${oauth2.client.kakao.user-info-uri}")
    private String kakaoUserInfo;

    @Value("${oauth2.client.naver.client-id}")
    private String naverClientId;

    @Value("${oauth2.client.naver.client-secret}")
    private String naverClientSecret;

    @Value("${oauth2.client.naver.token-uri}")
    private String naverTokenUri;

    @Value("${oauth2.client.naver.user-info-uri}")
    private String naverUserInfo;

    @Value("${oauth2.client.google.client-id}")
    private String googleClientId;

    @Value("${oauth2.client.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth2.client.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${oauth2.client.google.token-uri}")
    private String googleTokenUri;

    @Value("${oauth2.client.google.user-info-uri}")
    private String googleUserInfo;

    public TokenInfo socialLogin(SocialType socialType, String socialAccessToken) {
        return switch (socialType) {
            case GOOGLE -> googleLogin(socialAccessToken, socialType);
            case NAVER -> naverLogin(socialAccessToken, socialType);
            case KAKAO -> kakaoLogin(socialAccessToken, socialType);
            default -> throw new IllegalArgumentException("Invalid Provider Type.");
        };
    }

    public TokenInfo kakaoLogin(String accessToken, SocialType socialType) {
        //String accessToken = getKakaoAccessToken(code);
        KakaoMember kakaoMember = getKakaoUserInfo(accessToken);
        Member member = createSocialUser(kakaoMember.getSocialId(), kakaoMember.getKakaoAccount().getNickName(), socialType);
        return jwtProvider.createToken(member.getId().toString(), "ROLE_" + member.getRole().toString());
    }

    public TokenInfo naverLogin(String accessToken, SocialType socialType) {
        //String accessToken = getNaverAccessToken(code);
        NaverMember naverMember = getNaverUserInfo(accessToken);
        Member member = createSocialUser(naverMember.getNaverUserInfo().getId(), naverMember.getNaverUserInfo().getName(), socialType);
        return jwtProvider.createToken(member.getId().toString(), "ROLE_" + member.getRole().toString());
    }

    public TokenInfo googleLogin(String accessToken, SocialType socialType) {
        //String accessToken = getGoogleAccessToken(code);
        GoogleMember googleMember = getGoogleUserInfo(accessToken);
        Member member = createSocialUser(googleMember.getSocialId(), googleMember.getSocialName(), socialType);
        return jwtProvider.createToken(member.getId().toString(), "ROLE_" + member.getRole().toString());
    }

    public String getKakaoAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","authorization_code");
        body.add("client_id", kakaoClientId);
        body.add("client_secret", kakaoClientSecret);
        body.add("redirect_uri", kakaoRedirectUri);
        body.add("code",code);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers);
        KakaoToken kakaoToken = restTemplate.postForObject(kakaoTokenUri,tokenRequest, KakaoToken.class);

        if (kakaoToken == null) {
            log.error("카카오 토큰 에러");
            return null;
        }
        return kakaoToken.getAccess_token();
    }



    public String getNaverAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","authorization_code");
        body.add("client_id", naverClientId);
        body.add("client_secret", naverClientSecret);
        body.add("code",code);
        //body.add("state","32142131");

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers);
        NaverToken naverToken = restTemplate.postForObject(naverTokenUri,tokenRequest, NaverToken.class);

        if (naverToken == null) {
            log.error("네이버 토큰 에러");
            return null;
        }

        return naverToken.getAccess_token();
    }

    public String getGoogleAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type","authorization_code");
        body.add("client_id", googleClientId);
        body.add("client_secret", googleClientSecret);
        body.add("code",code);
        body.add("redirect_uri",googleRedirectUri);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(body, headers);
        GoogleToken googleToken = restTemplate.postForObject(googleTokenUri,tokenRequest, GoogleToken.class);

        if (googleToken == null) {
            log.error("구글 토큰 에러");
            return null;
        }

        return googleToken.getAccess_token();
    }

    public KakaoMember getKakaoUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "Bearer " + accessToken);

        //헤더 + 바디
        HttpEntity<MultiValueMap<String, String>> memberInfoRequest = new HttpEntity<>(headers);
        ResponseEntity<KakaoMember> kakaoMember = restTemplate.exchange(kakaoUserInfo, HttpMethod.GET, memberInfoRequest, KakaoMember.class);
        return kakaoMember.getBody();
    }

    public NaverMember getNaverUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "Bearer " + accessToken);

        //헤더 + 바디
        HttpEntity<MultiValueMap<String, String>> memberInfoRequest = new HttpEntity<>(headers);
        ResponseEntity<NaverMember> naverMember = restTemplate.exchange(naverUserInfo, HttpMethod.GET, memberInfoRequest, NaverMember.class);

        return naverMember.getBody();
    }

    public GoogleMember getGoogleUserInfo(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("Authorization", "Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> memberInfoRequest = new HttpEntity<>(headers);
        ResponseEntity<GoogleMember> googleMember = restTemplate.exchange(googleUserInfo, HttpMethod.GET, memberInfoRequest, GoogleMember.class);

        return googleMember.getBody();
    }

    @Transactional
    public Member createSocialUser(String socialId, String socialName, SocialType socialType) {
        if (!socialMemberRepository.existsBySocialId(socialId)) {
            Member member = new Member(socialName, RoleType.USER);
            return socialMemberRepository.save(new SocialMember(socialId, socialType, member)).getMember();
        }
        SocialMember socialMember = findBySocialId(socialId);
        return socialMember.getMember();
    }

    @Transactional(readOnly = true)
    public SocialMember findBySocialId(String socialId) {
        return socialMemberRepository.findBySocialId(socialId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 소셜id가 존재하지 않습니다."));
    }
}
