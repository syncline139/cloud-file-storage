package com.example.project.exceptions;

import com.example.project.dto.response.ErrorResponse;
import com.example.project.exceptions.auth.*;
import com.example.project.exceptions.storage.MissingOrInvalidPathException;
import com.example.project.exceptions.storage.PathNotFoundException;
import com.example.project.exceptions.storage.ResourceAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Класс отвечает за поимку всех исключений помеченными @RestController
 * он их обрабатывает и отпрвляет пользотвалю JSON c ошибкой
 */
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    // ошибки валидации (пример - слишком короткий login)
    @ExceptionHandler(UserNotValidationException.class)
    public ResponseEntity<ErrorResponse> handleException(UserNotValidationException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, status); // 400
    }

    // login занят
    @ExceptionHandler(UniqueLoginException.class)
    public ResponseEntity<ErrorResponse> handleException(UniqueLoginException e) {
        HttpStatus status = HttpStatus.CONFLICT;

        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, status); // 409
    }

    // неизвестная ошибка
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, status); // 500
    }

    // пользователь не авторизован
    @ExceptionHandler(LoginExistenceException.class)
    public ResponseEntity<ErrorResponse> handleException(LoginExistenceException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, status); //401
    }

    // Неверные данные при аутентификации
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleException(BadCredentialsException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ErrorResponse response = new ErrorResponse(
                 "Неверный логин или пароль",
                status.value(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, status); //401
    }

    //Пользователь не авторизирован
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleException(AuthenticationCredentialsNotFoundException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, status); //401
    }

    @ExceptionHandler(PathNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePathNotFound(PathNotFoundException e) {

        HttpStatus status = HttpStatus.NOT_FOUND;

        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(SpongeBobSquarePants.class)
    public ResponseEntity<ErrorResponse> handleException(SpongeBobSquarePants e) {
        HttpStatus status = HttpStatus.I_AM_A_TEAPOT;
        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(MissingOrInvalidPathException.class)
    public ResponseEntity<ErrorResponse> handleException(MissingOrInvalidPathException e) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, status);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleException(ResourceAlreadyExistsException e) {
        HttpStatus status = HttpStatus.CONFLICT;

        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, status);
    }

}
