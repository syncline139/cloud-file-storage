package com.example.project.utils;

import com.example.project.exceptions.auth.UserNotValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class BindingResultValidator {

    /**
     * Валидация
     * <p>
     *     В слачаее не пройденной валидации собирает ошибку и кидает {@link UserNotValidationException} с готовым сообщением
     * </p>
     * @param bindingResult получаем ошибки пришедшие с сущности
     */
    public void checkForValidationErrors(BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            StringBuilder errorMsg = new StringBuilder();
            List<FieldError> errors = bindingResult.getFieldErrors();
            for (FieldError error : errors) {
                errorMsg
                        .append(error.getDefaultMessage())
                        .append(";");
            }
            log.warn("Запрос от пользователя не прошел валидацию: {}", errorMsg);
            throw new UserNotValidationException(errorMsg.toString());
        }
        log.info("Запрос от пользователя прошел валидацию");

    }
}
