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

    // ошибки валидации (пример - слишком короткий login)
    @ExceptionHandler
    public ResponseEntity<UserErrorResponse> handleException(UserNotValidationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        UserErrorResponse response = new UserErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, status); // 400
    }

    // login занят
    @ExceptionHandler
    public ResponseEntity<UserErrorResponse> handleException(UniqueLoginException e) {
        HttpStatus status = HttpStatus.CONFLICT;

        UserErrorResponse response = new UserErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, status); // 409
    }

    // неизвестная ошибка
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

    // пользователь не авторизован
    @ExceptionHandler
    public ResponseEntity<UserErrorResponse> handleException(LoginExistenceException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        UserErrorResponse response = new UserErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, status);
    }

}
