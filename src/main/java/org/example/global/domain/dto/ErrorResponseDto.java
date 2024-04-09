package org.example.global.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponseDto {
    //private String errorCode;
    private String message; //일단 메세지만

}
