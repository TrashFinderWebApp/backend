package org.example.domain.rank.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PersonalRankResponse {
    private Long personalRank;
    private Long totalPeople;
    private Long personalPoint;

}
