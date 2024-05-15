package org.example.domain.admin.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.admin.controller.dto.response.MemberListResponse;
import org.example.domain.member.domain.Member;
import org.example.domain.member.repository.MemberRepository;
import org.example.domain.member.type.RoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final MemberRepository memberRepository;

    public MemberListResponse getMembersList(Integer page) {
        PageRequest pageRequest = PageRequest.of(page, 20);
        Page<Member> member = memberRepository.findAll(pageRequest);

        return new MemberListResponse(member);
    }

    public MemberListResponse getMembersListFindByName(Integer page, String memberName) {
        PageRequest pageRequest = PageRequest.of(page, 20);
        Page<Member> member = memberRepository.findByName(memberName, pageRequest);

        return new MemberListResponse(member);
    }

    @Transactional
    public void updateMemberRole(Long memberId, String updatedRole) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("유저 id가 존재하지 않습니다."));
        member.updateRole(RoleType.valueOf(updatedRole));
    }
}
