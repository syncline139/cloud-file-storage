package com.example.project.exceptions;

import com.example.project.dto.response.ErrorResponse;
import com.example.project.exceptions.auth.*;
import com.example.project.exceptions.storage.MissingOrInvalidPathException;
import com.example.project.exceptions.storage.PathNotFoundException;
import com.example.project.exceptions.storage.ResourceAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {



    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationException(MethodArgumentNotValidException ex) {
        StringBuilder errorMsg = new StringBuilder();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMsg
                    .append(error.getDefaultMessage())
                    .append("; ");
        });

        return new ResponseEntity<>(errorMsg.toString(), HttpStatus.BAD_REQUEST);
    }

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

    @ExceptionHandler(UniqueUsernameException.class)
    public ResponseEntity<ErrorResponse> handleException(UniqueUsernameException e) {
        HttpStatus status = HttpStatus.CONFLICT;

        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, status); // 409
    }

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

    @ExceptionHandler(UsernameExistenceException.class)
    public ResponseEntity<ErrorResponse> handleException(UsernameExistenceException e) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        ErrorResponse response = new ErrorResponse(
                e.getMessage(),
                status.value(),
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, status); //401
    }

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
