package com.example.project.annotations.storage;

import com.example.project.dto.response.ErrorResponse;
import com.example.project.dto.response.ResourceInfoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
        summary = "Поиск ресурса",
        description = "Возвращает все найденные совпадения в имени по запросу."
)
@Parameter(
        name = "query",
        description = "Поисковый запрос в URL-encoded формате.",
        required = true,
        in = ParameterIn.QUERY,
        schema = @Schema(type = "string", example = "Диплом")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Найденные ресурсы по запросу",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ResourceInfoResponse.class),
                        examples = @ExampleObject(
                                value = """
                                        [
                                          {
                                            "path": "folder1/folder2/",
                                            "name": "Диплом_Алеша.docx",
                                            "size": 123,
                                            "type": "FILE"
                                          },
                                          {
                                            "path": "folder1/folder2/",
                                            "name": "Пальмы/",
                                            "type": "DIRECTORY"
                                          }
                                        ]
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Невалидный или отсутствующий поисковой запрос",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                value = """
                    {
                      "message": "Невалидный или отсутствующий поисковой запрос",
                      "statusCode": 400,
                      "timestamp": 1713203213000
                    }
                    """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "401",
                description = "Пользователь не авторизован",
                content = @Content(
                        mediaType = "application/json",
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
                      "message": "Ошибка при получении информации о ресурсе",
                      "statusCode": 500,
                      "timestamp": 1713203213000
                    }
                    """
                        )
                )
        )
})

public @interface SearchResourceDoc {
}
