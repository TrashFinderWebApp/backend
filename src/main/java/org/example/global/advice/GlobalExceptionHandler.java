package org.example.global.advice;

import io.jsonwebtoken.ExpiredJwtException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import org.example.domain.trashcan.exception.MemberNotFoundException;
import org.example.domain.trashcan.exception.TrashcanNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(basePackages = "org.example.domain")
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessage> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        String message = fieldErrors.stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorMessage errorMessage = new ErrorMessage(message);
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({
            RuntimeException.class,
            ServletRequestBindingException.class // Including MissingServletRequestParameterException
    })
    public ResponseEntity<ErrorMessage> handleCustomExceptions(Exception e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        if (e instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException ex = (MissingServletRequestParameterException) e;
            String message = String.format("필수 파라미터 '%s' (%s)이(가) 누락되었습니다.", ex.getParameterName(), ex.getParameterType());
            return ResponseEntity.badRequest().body(new ErrorMessage(message));
        } else if (e instanceof ExpiredJwtException) {
            status = HttpStatus.FORBIDDEN;
        } else if (e instanceof TrashcanNotFoundException || e instanceof MemberNotFoundException) {
            status = HttpStatus.NOT_FOUND;
        }
        return new ResponseEntity<>(new ErrorMessage(e.getMessage()), status);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorMessage> handleTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String message = "잘못된 파라미터 형식입니다.";
        ErrorMessage errorMessage = new ErrorMessage(message);
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorMessage> handleNoSuchElementException(NoSuchElementException e) {
        ErrorMessage errorMessage = new ErrorMessage("요청한 리소스를 찾을 수 없습니다.");
        return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessage> handleAll(Exception ex) {
        ErrorMessage errorMessage = new ErrorMessage("서버 오류가 발생했습니다. 관리자에게 문의하세요.");
        return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
