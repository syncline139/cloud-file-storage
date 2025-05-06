package com.example.project.annotations.profile;

import com.example.project.dto.response.ErrorResponse;
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
        summary = "Информация об активном пользователе",
        description = "Выдает пользотвалю его логин"
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Пользователь успешно получил информацию о себе",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(example = "{\"username\": \"user_1\"}")
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "пользователь не авторизован",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                value = """
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
                        schema = @Schema(implementation = ErrorResponse.class),
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
public @interface UserInfoDoc {
}
