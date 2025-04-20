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
        summary = "Выход пользователя",
        description = "Пользователь выходит с активнов сессии"
)
@ApiResponses(
        value = {
                @ApiResponse(
                        responseCode = "204",
                        description = "Завершает активную сессию пользователя (выход из системы)"
                ),
                @ApiResponse(
                        responseCode = "401",
                        description = "Запрос исполняется неавторизованным юзером",
                        content = @Content(mediaType = "application/json",
                                schema = @Schema(implementation = UserErrorResponse.class),
                                examples = @ExampleObject(
                                        """
                                        {
                                          "message": "Пользователь не авторизован",
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
        }
)
public @interface UserSignOutDoc {

}
