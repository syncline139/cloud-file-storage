package com.example.project.annotations.auth;

import com.example.project.dto.response.UserErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Регистрация нового пользователя",
        description = "Создаёт нового пользователя, проводит валидацию, сохраняет в БД и создаёт сессию"
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Пользователь успешно зарегистрирован",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(example = "{\"login\": \"user_1\"}")
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Ошибки валидации (пример - слишком короткий username)",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UserErrorResponse.class),
                        examples = @ExampleObject(
                                value = """
                                        {
                                          "message": "Логин должен быть в диапазоне от 5 до 30 символов",
                                          "statusCode": 400,
                                          "timestamp": 1713203213000
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "409",
                description = "Ошибка кастомной валидации(проверка на уникальность логина)",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UserErrorResponse.class),
                        examples = @ExampleObject(
                                value = """
                                        {
                                          "message": "Пользователь с логином 'user_1' уже зарегистрирован",
                                          "statusCode": 409,
                                          "timestamp": 1713203213000
                                        }
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "500",
                description = "Внутренняя ошибка сервера",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = UserErrorResponse.class),
                        examples = @ExampleObject(
                                value = """
                                        {
                                          "message": "Не удалось подключится к базе данных",
                                          "statusCode": 500,
                                          "timestamp": 1713203213000
                                        }
                                        """
                        )
                )
        )
})
public @interface UserSignUpDoc {

}
