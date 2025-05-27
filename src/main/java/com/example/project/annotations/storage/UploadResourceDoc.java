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
        summary = "Загрузка ресурсов",
        description = "Загружает один или несколько файлов/папок. В имени файла может быть указана поддиректория для создания вложенных структур."
)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "201",
                description = "Ресурсы успешно загружены",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ResourceInfoResponse.class),
                        examples = @ExampleObject(
                                value = """
                                        [
                                          {
                                            "path": "folder1/upload_target/",
                                            "name": "image.jpg",
                                            "size": 10240,
                                            "type": "FILE"
                                          },
                                          {
                                            "path": "folder1/upload_target/new_folder/",
                                            "name": "document.pdf",
                                            "size": 5120,
                                            "type": "FILE"
                                          }
                                        ]
                                        """
                        )
                )
        ),
        @ApiResponse(
                responseCode = "400",
                description = "Невалидный путь или некорректное тело запроса",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                value = """
                    {
                      "message": "Невалидный путь или неверный формат запроса",
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
                responseCode = "409",
                description = "Ресурс с таким именем уже существует в целевой папке",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                value = """
                    {
                      "message": "Файл 'existing_file.txt' уже существует",
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
                        schema = @Schema(implementation = ErrorResponse.class),
                        examples = @ExampleObject(
                                value = """
                    {
                      "message": "Ошибка при загрузке ресурса",
                      "statusCode": 500,
                      "timestamp": 1713203213000
                    }
                    """
                        )
                )
        )
})
public @interface UploadResourceDoc {
}
