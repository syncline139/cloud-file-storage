package com.example.project.utils;

import com.example.project.exceptions.UserNotValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

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
            throw new UserNotValidationException(errorMsg.toString());
        }

    }
}
