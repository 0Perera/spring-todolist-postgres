package dio.todolist.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Endereço de e-mail do usuário para realizar o login.", example = "felipe@example.com")
        @Email(message = "O email deve ser válido")
        @NotBlank(message = "O email é obrigatório")
        String email,
        @Schema(description = "Senha do usuário para realizar o login.", example = "senhaForte123")
        @NotBlank(message = "A senha é obrigatória")
        String password
) {
}
