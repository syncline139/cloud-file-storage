package com.example.project.exceptions;

import com.example.project.dto.response.UserErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice()
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<UserErrorResponse> handleException(UserNotCreatedException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        UserErrorResponse response = new UserErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, status); // 400
    }
}
