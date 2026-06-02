package dio.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserResponse(
        @Schema(description = "Identificador único do usuário.", example = "1")
        Long id,

        @Schema(description = "Nome de exibição do usuário.", example = "Felipe")
        String name,

        @Schema(description = "Endereço de e-mail do usuário.", example = "felipe@example.com")
        String email
) {}
