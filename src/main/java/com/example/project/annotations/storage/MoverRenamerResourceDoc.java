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
        summary = "Переименование/перемещение ресурса",
        description = ""
)
@Parameter(
        name = "from",
        description = "Полный путь к старому ресурсу в URL-encoded формате (например, 'folder1/folder2/' для папки или 'folder1/file.txt' для файла). Путь к папке должен заканчиваться на '/'.",
        required = true,
        in = ParameterIn.QUERY,
        schema = @Schema(type = "string", example = "folder1/folder2/")
)
@Parameter(
        name = "to",
        description = "Полный путь к новому ресурсу в URL-encoded формате (например, 'folder1/folder3/' для папки или 'folder1/doc.txt' для файла). Путь к папке должен заканчиваться на '/'.",
        required = true,
        in = ParameterIn.QUERY,
        schema = @Schema(type = "string", example = "folder1/folder2/")
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "200",
                description = "Переименование/перемещение ресурса прошло успешно",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ResourceInfoResponse.class),
                        examples = @ExampleObject(
                                value = """
                    {
                      "path": "folder1/",
                      "name": "doc.txt",
                      "size": 123,
                      "type": "FILE"
                    }
                    """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Невалидный или отсутствующий путь",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                value = """
                    {
                      "message": "Невалидный или отсутствующий путь",
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
                responseCode = "404",
                description = "Ресурс не найден",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                value = """
                    {
                      "message": "Ресурс не найден",
                      "statusCode": 404,
                      "timestamp": 1713203213000
                    }
                    """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "409",
                description = "ресурс, лежащий по пути 'to' уже существует",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                value = """
                                        {
                                            "message": "Ресурс, лежащий по пути 'folder1/doc.txt' уже существует",
                                            "statusCode": 409,
                                            "timestamp": 1748305225641
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
public @interface MoverRenamerResourceDoc {
}
