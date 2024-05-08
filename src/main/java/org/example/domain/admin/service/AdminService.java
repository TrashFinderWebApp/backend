package org.example.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.admin.controller.dto.response.MemberListResponse;
import org.example.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final MemberRepository memberRepository;

    public MemberListResponse getMembersList() {
        MemberListResponse memberListResponse = new MemberListResponse();
        return memberListResponse;
    }
}
