package org.example.domain.trashcan.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrashcanLocationRequest {

    private double latitude;
    private double longitude;
    private double radius;
    private String status;
}
