package ru.practicum.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException e) {
        return new ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        FieldError error = e.getBindingResult().getFieldError();
        String reason = "Incorrectly made request.";
        String message = error != null
                ? String.format("Field: %s. Error: %s. Value: %s", error.getField(), error.getDefaultMessage(),
                error.getRejectedValue())
                : reason;
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason(reason)
                .message(message)
                .errors(List.of())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleUniqueViolation(DataIntegrityViolationException e) {
        String reason = "Incorrectly made request.";
        String message = e.getMessage();
        return ApiError.builder()
                .status(HttpStatus.CONFLICT.name())
                .reason(reason)
                .message(message)
                .errors(List.of())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
