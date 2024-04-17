package org.example.global.advice;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorMessage {
    @JsonProperty("message")
    private String message;
}
