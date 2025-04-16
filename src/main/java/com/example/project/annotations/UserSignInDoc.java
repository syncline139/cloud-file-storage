package com.example.project.annotations;


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


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(
        summary = "Аутентификация пользотваля",
        description = "Аутентифицирует пользотваля после валидации создавая ему сессию"
)
@ApiResponses(
        value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Пользователь успешно вошел",
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
                        responseCode = "401",
                        description = "Неверные данные (такого пользователя нет, или пароль неправильный)",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = UserErrorResponse.class),
                                examples = @ExampleObject(
                                        value = """
                                        {
                                          "message": "Неверный логин или пароль",
                                          "statusCode": 401,
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
public @interface UserSignInDoc {
}
