package com.example.project.annotations.profile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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
                        schema = @Schema(example = "{\"login\": \"user_1\"}")
                )
        )
}
)
public @interface UserInfoDoc {
}
