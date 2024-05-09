package org.example.domain.admin.controller.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.domain.member.domain.Member;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class MemberListResponse {
    private Integer totalPages;
    private List<MemberInfo> memberInfoList;

    @Getter
    @AllArgsConstructor
    private class MemberInfo {
        private Long memberId;
        private String memberName;
        private LocalDateTime registerDate;
        private String memberStatus;
    }

    public MemberListResponse(Page<Member> members) {
        this.memberInfoList = new ArrayList<>();
        this.totalPages = members.getTotalPages();

        for (Member member : members) {
            MemberInfo memberInfo = new MemberInfo(member.getId(), member.getName(),
                    member.getCreatedAt(), member.getRole().name());

            this.memberInfoList.add(memberInfo);
        }
    }




}
