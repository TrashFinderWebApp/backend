package org.example.global.advice;

import java.util.List;
import javax.naming.AuthenticationException;
import org.example.global.domain.dto.ErrorResponseDto;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.webjars.NotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 잘못된 파라미터 값 예외 처리
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        ErrorMessage errorMessage = new ErrorMessage("잘못된 파라미터 값입니다: " + e.getName());
        return ResponseEntity.badRequest().body(errorMessage);
    }

    // 지원하지 않는 상태 값 예외 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorMessage> handleIllegalArgumentException(IllegalArgumentException e) {
        ErrorMessage errorMessage = new ErrorMessage("지원하지 않는 상태 값입니다.");
        return ResponseEntity.badRequest().body(errorMessage);
    }

    // 데이터베이스 접근 오류 처리
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorMessage> handleDataAccessException(DataAccessException e) {
        ErrorMessage errorMessage = new ErrorMessage("데이터베이스 접근 중 오류가 발생했습니다.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }

    // 데이터 미존재 예외 처리
    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ErrorMessage> handleEmptyResultDataAccessException(EmptyResultDataAccessException e) {
        ErrorMessage errorMessage = new ErrorMessage("조건에 맞는 데이터가 없습니다.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
    }

    //인증 실패 처리
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorMessage> handleAuthenticationException(AuthenticationException e) {
        ErrorMessage errorMessage = new ErrorMessage("인증 실패: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMessage);
    }

    // ResponseStatusException 처리
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorMessage> handleResponseStatusException(ResponseStatusException e) {
        ErrorMessage errorMessage = new ErrorMessage(e.getReason());
        return new ResponseEntity<>(errorMessage, e.getStatusCode());
    }
}
