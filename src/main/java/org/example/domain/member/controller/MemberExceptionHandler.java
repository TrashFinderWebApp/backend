package org.example.domain.member.controller;

import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.example.global.domain.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class MemberExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseEntity<ErrorResponseDto> onConstraintValidationException(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        FieldError fieldError = fieldErrors.get(fieldErrors.size()-1);
        //String fieldName = fieldError.getField();
        //Object rejectedValue = fieldError.getRejectedValue();

        return new ResponseEntity<>(new ErrorResponseDto(fieldError.getDefaultMessage()),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleMemberValidationException2(Exception e) {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;

        return new ResponseEntity<>(e.getMessage(), httpStatus);
    }
}
