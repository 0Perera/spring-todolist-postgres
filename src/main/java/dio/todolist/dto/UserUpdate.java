package dio.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Optional;

public record UserUpdate(
        @Schema(description = "Novo nome de exibição. Omita o campo para não alterar.", example = "Felipe Dev")
        Optional<String> name,

        @Schema(description = "Novo e-mail de acesso. Omita o campo para não alterar.", example = "feeps@example.com")
        Optional<String> email,

        @Schema(description = "Nova senha de acesso. Omita o campo para não alterar.", example = "novaSenha456")
        Optional<String> password
) {}
