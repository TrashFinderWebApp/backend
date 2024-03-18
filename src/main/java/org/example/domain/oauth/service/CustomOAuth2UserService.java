package org.example.domain.oauth.service;

import lombok.RequiredArgsConstructor;
import org.example.domain.oauth.domain.OAuth2UserInfo;
import org.example.domain.oauth.domain.OAuth2UserInfoFactory;
import org.example.domain.user.domain.User;
import org.example.domain.user.service.UserService;
import org.example.domain.user.type.AuthType;
import org.example.domain.user.type.RoleType;
import org.example.global.security.authentication.CustomUserDetails;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> oAuth2UserService = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = oAuth2UserService.loadUser(userRequest);
        return null;
        //return processOAuth2User(userRequest, oAuth2User);
    }

    protected OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        //OAuth2 로그인 플랫폼 구분
        AuthType authType = AuthType.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase());
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(authType, oAuth2User.getAttributes());

        User user = userService.findBySocialId(oAuth2UserInfo.getOAuth2Id());
        //이미 가입된 경우
        if (user != null) {
            if (!user.getAuthType().equals(authType)) {
                throw new RuntimeException("already signed up.");
            }
            user = updateUser(user, oAuth2UserInfo);
        }
        //가입되지 않은 경우
        else {
            user = registerUser(authType, oAuth2UserInfo);
        }
        return CustomUserDetails.create(user, oAuth2UserInfo.getAttributes());
    }

    private User registerUser(AuthType authType, OAuth2UserInfo oAuth2UserInfo) {
        User user = User.builder()
                .email(oAuth2UserInfo.getEmail())
                .name(oAuth2UserInfo.getName())
                .socialId(oAuth2UserInfo.getOAuth2Id())
                .authType(authType)
                .role(RoleType.ROLE_USER)
                .build();

        return userService.saveUser(user);
    }

    private User updateUser(User user, OAuth2UserInfo oAuth2UserInfo) {
        return userService.saveUser(user.update(
                oAuth2UserInfo.getEmail(),
                oAuth2UserInfo.getName(),
                oAuth2UserInfo.getOAuth2Id()
        ));
    }
}
