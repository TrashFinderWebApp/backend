package org.example.global.advice;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.AuthenticationException;
import org.example.domain.trashcan.exception.InvalidStatusException;
import org.example.domain.trashcan.exception.TrashcanNotFoundException;
import org.example.global.domain.dto.ErrorResponseDto;
import org.hibernate.TypeMismatchException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.webjars.NotFoundException;

@RestControllerAdvice(basePackages = "org.example.domain.trashcan")
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidStatusException.class)
    public ResponseEntity<ErrorMessage> handleInvalidStatusException(InvalidStatusException e) {
        ErrorMessage errorMessage = new ErrorMessage(e.getMessage());
        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(TrashcanNotFoundException.class)
    public ResponseEntity<ErrorMessage> handleTrashcanNotFoundException(TrashcanNotFoundException e) {
        ErrorMessage errorMessage = new ErrorMessage(e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorMessage);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors();

        Map<String, String> errors = new HashMap<>();
        fieldErrors.forEach(f -> errors.put(f.getField(), f.getDefaultMessage()));

        return ResponseEntity
                .badRequest()
                .body(errors);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorMessage> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        String message = String.format("필수 파라미터 '%s' (%s)이(가) 누락되었습니다.", ex.getParameterName(), ex.getParameterType());
        ErrorMessage errorMessage = new ErrorMessage(message);
        return ResponseEntity.badRequest().body(errorMessage);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorMessage> handleTypeMismatchException(TypeMismatchException ex, WebRequest request) {
        ErrorMessage errorMessage = new ErrorMessage("잘못된 파라미터 형식입니다.");
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }
}
