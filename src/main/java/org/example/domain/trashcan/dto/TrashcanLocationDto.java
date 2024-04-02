package org.example.domain.trashcan.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrashcanLocationDto {
    private Long trashcanId;
    private double latitude;
    private double longitude;
    private String addressDetail;
}
