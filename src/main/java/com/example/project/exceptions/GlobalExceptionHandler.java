package com.example.project.exceptions;

import com.example.project.dto.response.UserErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;


/**
 * Класс отвечает за поимку всех исключений помеченными @RestController
 * он их обрабатывает и отпрвляет пользотвалю JSON c ошибкой
 */
@ControllerAdvice(annotations = RestController.class)
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

    @ExceptionHandler
    public ResponseEntity<UserErrorResponse> handleException(LoginConflictException e) {
        HttpStatus status = HttpStatus.CONFLICT;

        UserErrorResponse response = new UserErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, status); // 409
    }

    @ExceptionHandler
    public ResponseEntity<UserErrorResponse> handleException(Exception e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        UserErrorResponse response = new UserErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, status); // 500
    }
}
